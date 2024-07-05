package edu.ivanuil.friendalertbot.repository;

import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseCredentials;
import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseProtocol;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseResponse;
import com.clickhouse.data.ClickHouseFormat;
import edu.ivanuil.friendalertbot.dto.VisitorDto;
import edu.ivanuil.friendalertbot.entity.TransitDirection;
import edu.ivanuil.friendalertbot.exception.ClickHouseClientException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class VisitorLogRepository {

    private final ClickHouseNode node;
    private final ClickHouseCredentials credentials;

    @PostConstruct
    public void createSchema() {
        createVisitorsLogTable();
        createVisitorsTransitLog();
    }

    private void createVisitorsLogTable() {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             var response = client.write(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query("""
                        CREATE TABLE IF NOT EXISTS visitors_count_log (
                                     timestamp TIMESTAMP,
                                     campus VARCHAR,
                                     cluster VARCHAR,
                                     visitors_count INTEGER)
                        ENGINE MergeTree
                        PRIMARY KEY (timestamp, campus, cluster);
                        """)
                     .executeAndWait()) {
            log.info("Created visitors_log table in ClickHouse");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

    private void createVisitorsTransitLog() {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             var response = client.write(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query("""
                        CREATE TABLE IF NOT EXISTS visitors_transit_log (
                                     timestamp TIMESTAMP,
                                     campus VARCHAR,
                                     cluster VARCHAR,
                                     place VARCHAR(2),
                                     login VARCHAR,
                                     direction VARCHAR)
                        ENGINE MergeTree
                        PRIMARY KEY (timestamp, login);
                        """)
                     .executeAndWait()) {
            log.info("Created visitors_transit_log table in ClickHouse");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

    @Retryable(retryFor = ClickHouseClientException.class, maxAttempts = 2, backoff = @Backoff(delay = 100))
    public void appendVisitorsCountLog(final String campus, final String cluster, final int visitorsCount) {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
            ClickHouseResponse response = client.write(node)
                    .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                    .query("""
                            INSERT INTO visitors_count_log(timestamp, campus, cluster, visitors_count)
                            VALUES (now(), :campus, :cluster, :visitorsCount);
                            """)
                    .params(campus, cluster, visitorsCount)
                    .executeAndWait()) {
            if (response.getSummary().getWrittenRows() != 1)
                throw new ClickHouseClientException("Error writing to visitors_count_log table");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

    @Retryable(retryFor = ClickHouseClientException.class, maxAttempts = 2, backoff = @Backoff(delay = 100))
    public void appendVisitorsEnteringAndLeaving(final Map<VisitorDto, TransitDirection> visitors) {
        StringBuilder query = new StringBuilder("""
                            INSERT INTO visitors_transit_log(timestamp, campus, cluster, login, direction, place)
                            VALUES
                            """);
        for (var visitor : visitors.entrySet()) {
            query.append(String.format("(now(), '%s', '%s', '%s', '%s', '%s%d'),",
                    visitor.getKey().getCampus(),
                    visitor.getKey().getCluster(),
                    visitor.getKey().getLogin(),
                    visitor.getValue().toString(),
                    visitor.getKey().getRow(),
                    visitor.getKey().getNumber()));
        }
        query.deleteCharAt(query.length() - 1);  // Remove last ','
        query.append(";");

        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             ClickHouseResponse response = client.write(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query(query.toString())
                     .executeAndWait()) {
            if (response.getSummary().getWrittenRows() != visitors.size())
                throw new ClickHouseClientException("Error writing to visitors_transit_log table");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

}
