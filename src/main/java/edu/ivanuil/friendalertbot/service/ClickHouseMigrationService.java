package edu.ivanuil.friendalertbot.service;

import com.clickhouse.client.*;
import com.clickhouse.data.ClickHouseFormat;
import edu.ivanuil.friendalertbot.dto.ClickHouseMigration;
import edu.ivanuil.friendalertbot.exception.ClickHouseMigrationsException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClickHouseMigrationService {

    private final ClickHouseNode node;
    private final ClickHouseCredentials credentials;

    @Value("${clickhouse.migrations-folder}")
    private String migrationsFolderPath;

    @PostConstruct
    public void applyMigrations() throws ClickHouseMigrationsException {
        createMigrationsTable();
        var migrationRecords = readMigrationTable();
        migrationRecords.sort(Comparator.comparingInt(ClickHouseMigration::getOrder));

        var migrationFiles = readMigrationFiles();
        Arrays.sort(migrationFiles, Comparator.comparing(File::getName));

        if (migrationRecords.size() > migrationFiles.length)
            throw new ClickHouseMigrationsException(
                    "Number of already applied migrations is bigger than number of migration files");

        var migrationsFromFiles = mapFilesToMigrations(migrationFiles);

        // Validating that first n migrations are present both in table and files
        // n - number of already performed migrations
        for (int i = 0; i < migrationFiles.length && i < migrationRecords.size(); i++) {
            var migrationFromTable = migrationRecords.get(i);
            if (migrationFromTable.assertEquals(migrationsFromFiles.get(i))) {
                throw new ClickHouseMigrationsException(
                        "Migrations already applied to db and migration files do not match");
            }
        }

        // Performing new migrations
        for (int i = migrationRecords.size(); i < migrationFiles.length; i++) {
            applyAndSaveMigration(migrationsFromFiles.get(i));
        }

    }

    private void createMigrationsTable() throws ClickHouseMigrationsException {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             var ignored = client.write(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query("""
                        CREATE TABLE IF NOT EXISTS migrations (
                                     name VARCHAR,
                                     filename VARCHAR,
                                     executed_at TIMESTAMP,
                                     order INTEGER,
                                     request VARCHAR)
                        ENGINE MergeTree
                        PRIMARY KEY (name);
                        """)
                     .executeAndWait()) {
            log.info("Successfully connected to ClickHouse, starting migrations");
        } catch (ClickHouseException e) {
            throw new ClickHouseMigrationsException(e);
        }
    }

    private List<ClickHouseMigration> readMigrationTable() throws ClickHouseMigrationsException {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             var response = client.read(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query("""
                        SELECT
                            name, filename, executed_at, order, request
                        FROM migrations
                        ORDER BY name ASC;
                        """)
                     .executeAndWait()) {

            var list = new LinkedList<ClickHouseMigration>();
            response.records().forEach(clickHouseValues ->
                    list.add(new ClickHouseMigration(
                        clickHouseValues.getValue("name").asString(),
                        clickHouseValues.getValue("filename").asString(),
                        clickHouseValues.getValue("executed_at").asDateTime(),
                        clickHouseValues.getValue("order").asInteger(),
                        clickHouseValues.getValue("request").asString())));
            return list;
        } catch (ClickHouseException e) {
            throw new ClickHouseMigrationsException(e);
        }
    }

    private File[] readMigrationFiles() throws ClickHouseMigrationsException {
        try {
            var dir = ResourceUtils.getFile(migrationsFolderPath);
            if (!dir.exists() || !dir.isDirectory())
                throw new ClickHouseMigrationsException("Migrations directory does not exist");

            return dir.listFiles();
        } catch (FileNotFoundException e) {
            throw new ClickHouseMigrationsException(e);
        }
    }

    private void applyAndSaveMigration(ClickHouseMigration migration) throws ClickHouseMigrationsException {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             var ignored = client.read(node).write()
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query(migration.getRequest())
                     .executeAndWait()) {
            migration.setExecutedAt(LocalDateTime.now());
        } catch (ClickHouseException e) {
            throw new ClickHouseMigrationsException(e);
        }

        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             var response = client.write(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query("""
                        INSERT INTO migrations(name, filename, executed_at, order, request)
                        VALUES (:name, :filename, FROM_UNIXTIME(:executed_at), :order, :request)
                        """)
                     .params(migration.getName(), migration.getFilename(),
                             migration.getExecutedAt().toInstant(ZoneOffset.MIN).toEpochMilli(), // todo: look into this
                             migration.getOrder(), migration.getRequest())
                     .executeAndWait()) {
            if (response.getSummary().getWrittenRows() != 1)
                throw new ClickHouseMigrationsException("Failed writing to migrations table");
        } catch (ClickHouseException e) {
            throw new ClickHouseMigrationsException(e);
        }
    }

    private static List<ClickHouseMigration> mapFilesToMigrations(File[] migrationFiles) {
        AtomicInteger count = new AtomicInteger(-1);
        return Arrays.stream(migrationFiles)
                .map(file -> {
                    Stream<String> requestLines;
                    try {
                        requestLines = new BufferedReader(new FileReader(file)).lines();
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    StringBuilder request = new StringBuilder();
                    requestLines
                            .filter(s -> !s.isEmpty() && !s.isBlank())
                            .forEach(request::append);

                    return new ClickHouseMigration(
                            file.getName().split("\\.")[0],
                            file.getName(),
                            null,
                            count.getAndIncrement() + 1,
                            request.toString());
                }).toList();
    }

}
