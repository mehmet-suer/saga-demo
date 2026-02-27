package com.saga.inventory.config;

import com.saga.common.idempotency.IdempotentEventService;
import com.saga.inventory.model.EventType;
import com.saga.inventory.repository.IdempotentEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotencyConfig {

    @Bean
    public IdempotentEventService<EventType> idempotentEventService(IdempotentEventRepository repository) {
        return new IdempotentEventService<>(repository);
    }
}
