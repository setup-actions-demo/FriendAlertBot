package edu.ivanuil.friendalertbot.configuration;

import lombok.SneakyThrows;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.clickhouse.ClickHouseContainer;

import java.time.Duration;
import java.util.regex.Pattern;

@SpringBootTest
@ContextConfiguration
public class ClickHouseTestConfiguration {

    private static volatile ClickHouseContainer container = null;

    private static ClickHouseContainer getClickHouseContainer() {
        ClickHouseContainer instance = container;
        if (instance == null) {
            synchronized (ClickHouseContainer.class) {
                instance = container;
                if (instance == null) {
                    container = instance = new ClickHouseContainer(
                            "clickhouse/clickhouse-server:23.3.8.21-alpine")
                            .withDatabaseName("local")
                            .withDatabaseName("username")
                            .withPassword("password")
                            .withUrlParam("compress", "0")
                            .withStartupTimeout(Duration.ofSeconds(5))
                            .withReuse(true);
                    container.start();
                }
            }
        }
        return instance;
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @SneakyThrows
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            var container = getClickHouseContainer();

            var jdbcUrl = container.getJdbcUrl();
            var url = "http://" + jdbcUrl.split("//")[1].split(Pattern.quote("?"))[0];
            var username = container.getUsername();
            var password = container.getPassword();

            TestPropertyValues.of(
                    "clickhouse.url=" + url,
                    "clickhouse.username=" + username,
                    "clickhouse.password=" + password
            ).applyTo(applicationContext.getEnvironment());
        }

    }

}
