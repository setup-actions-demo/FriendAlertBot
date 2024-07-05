package edu.ivanuil.friendalertbot;


import edu.ivanuil.friendalertbot.configuration.ClickHouseTestConfiguration;
import edu.ivanuil.friendalertbot.configuration.PostgreSQLTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest()
@ContextConfiguration(initializers = {
        PostgreSQLTestConfiguration.Initializer.class,
        ClickHouseTestConfiguration.Initializer.class
})
public class TestContext {

    @Test
    public void contextLoads() {

    }

}