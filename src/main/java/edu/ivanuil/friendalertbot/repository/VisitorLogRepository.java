package edu.ivanuil.friendalertbot.repository;

import com.clickhouse.client.*;
import com.clickhouse.data.ClickHouseFormat;
import edu.ivanuil.friendalertbot.exception.ClickHouseClientException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VisitorLogRepository {

    ClickHouseNodes nodes = ClickHouseNodes.of("http://localhost:8123?compress=0");
    ClickHouseNode node = ClickHouseNode.of("http://localhost:8123?compress=0");
    ClickHouseCredentials credentials = ClickHouseCredentials
            .fromUserAndPassword("clickhouse", "clickhouse");

    @PostConstruct
    public void createSchema() {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
        var response = client.write(node)
                .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                .query("""
                        CREATE TABLE IF NOT EXISTS visitors_log (
                                     timestamp TIMESTAMP PRIMARY KEY,
                                     campus VARCHAR PRIMARY KEY,
                                     cluster VARCHAR PRIMARY KEY,
                                     visitors_count INTEGER);
                        """)
                .executeAndWait()) {
            log.info("Created visitors_log table in ClickHouse");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

    @Retryable(retryFor = ClickHouseClientException.class, maxAttempts = 2, backoff = @Backoff(delay = 100))
    public void appendLog(String campus, String cluster, int visitorsCount) {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
            ClickHouseResponse response = client.write(node)
                    .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                    .query("""
                            INSERT INTO visitors_log(timestamp, campus, cluster, visitors_count)
                            VALUES (now(), :campus, :cluster, :visitorsCount);
                            """)
                    .params(campus, cluster, visitorsCount)
                    .executeAndWait()) {
            if (response.getSummary().getWrittenRows() != 1)
                throw new ClickHouseClientException("Error writing to visitors_log table");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

}
