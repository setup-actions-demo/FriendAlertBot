package edu.ivanuil.friendalertbot.repository;

import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseCredentials;
import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseProtocol;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseResponse;
import com.clickhouse.data.ClickHouseFormat;
import edu.ivanuil.friendalertbot.exception.ClickHouseClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class VisitorCountLogRepository {

    private final ClickHouseNode node;
    private final ClickHouseCredentials credentials;

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

}
