package edu.ivanuil.friendalertbot.repository;

import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.client.ClickHouseCredentials;
import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseProtocol;
import com.clickhouse.client.ClickHouseException;
import com.clickhouse.client.ClickHouseResponse;
import com.clickhouse.data.ClickHouseFormat;
import edu.ivanuil.friendalertbot.entity.ParticipantEntity;
import edu.ivanuil.friendalertbot.exception.ClickHouseClientException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipantInfoLogRepository {

    private final ClickHouseNode node;
    private final ClickHouseCredentials credentials;

    @PostConstruct
    public void createSchema() {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             var response = client.write(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query("""
                        CREATE TABLE IF NOT EXISTS participant_info_log (
                            login VARCHAR,
                            class_name VARCHAR,
                            parallel_name VARCHAR,
                            exp_value INTEGER,
                            level INTEGER,
                            exp_to_next_level INTEGER,
                            campus VARCHAR,
                            status VARCHAR,
                            updated_at TIMESTAMP)
                        ENGINE MergeTree
                        PRIMARY KEY (login);
                        """)
                     .executeAndWait()) {
            log.info("Created participant_info_log table in ClickHouse");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

    public void appendParticipantInfoLog(final Collection<ParticipantEntity> participants) {
        try (ClickHouseClient client = ClickHouseClient.newInstance(credentials, ClickHouseProtocol.HTTP);
             ClickHouseResponse response = client.write(node)
                     .format(ClickHouseFormat.RowBinaryWithNamesAndTypes)
                     .query(getRequest(participants))
                     .executeAndWait()) {
            if (response.getSummary().getWrittenRows() != participants.size())
                throw new ClickHouseClientException("Error writing to participant_info_log table");
        } catch (ClickHouseException e) {
            throw new ClickHouseClientException(e);
        }
    }

    private static String getRequest(final Collection<ParticipantEntity> participants) {
        StringBuilder builder = new StringBuilder("""
                INSERT INTO participant_info_log (
                    login, class_name, parallel_name,
                    exp_value, level, exp_to_next_level,
                    campus, status,
                    updated_at)
                VALUES
                """);
        participants.forEach(entity -> builder.append(String.format(
                "('%s', '%s', '%s', %d, %d, %d, '%s', '%s', FROM_UNIXTIME(%d)),\n",
                entity.getLogin(), entity.getClassName(), entity.getParallelName(),
                entity.getExpValue(), entity.getLevel(), entity.getExpToNextLevel(),
                entity.getCampus(), entity.getStatus(), entity.getUpdatedAt().getTime() / 1000L)));
        builder.deleteCharAt(builder.length() - 1);
        builder.append(";");
        return builder.toString();
    }

}
