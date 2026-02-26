package com.saga.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.saga.order.model.constant.ExecutorConstants;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {

    @Bean(name = ExecutorConstants.OUTBOX_EXECUTOR)
    public ExecutorService outboxExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean(name = ExecutorConstants.TIMED_OUT_ORDERS_EXECUTOR)
    public ExecutorService timedOutOrdersExecutor() {
        return Executors.newFixedThreadPool(10);
    }
}