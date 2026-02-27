package com.saga.payment.config;

import com.saga.common.idempotency.IdempotentEventService;
import com.saga.payment.model.EventType;
import com.saga.payment.repository.IdempotentEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdempotencyConfig {

    @Bean
    public IdempotentEventService<EventType> idempotentEventService(IdempotentEventRepository repository) {
        return new IdempotentEventService<>(repository);
    }
}
