package edu.ivanuil.friendalertbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableRetry
@EnableScheduling
@EnableAsync(proxyTargetClass=true)
public class FriendAlertBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(FriendAlertBotApplication.class, args);
    }

}
