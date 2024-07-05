package edu.ivanuil.friendalertbot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class SchedulingConfiguration {

    @Bean
    public Executor jobPool() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(3);
        exec.setMaxPoolSize(3);
        exec.setQueueCapacity(0);
        exec.setThreadNamePrefix("scheduling-");
        exec.initialize();
        return exec;
    }

}
