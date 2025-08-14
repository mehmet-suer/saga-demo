package com.saga.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = "outboxExecutor")
    public ExecutorService outboxExecutor() {
        return Executors.newFixedThreadPool(4);
    }
}