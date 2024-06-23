package edu.ivanuil.friendalertbot;

import org.springframework.boot.SpringApplication;

public class TestFriendAlertBotApplication {

    public static void main(String[] args) {
        SpringApplication.from(FriendAlertBotApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
