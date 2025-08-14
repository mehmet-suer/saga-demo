package com.saga.inventory.config;


import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaConfig {

    @Bean(name = "kafkaTopicProperties")
    public KafkaTopicProperties kafkaTopicProperties(KafkaTopicProperties props) {
        return props;
    }

    @Bean
    ProducerFactory<String, String> defaultProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("defaultKafkaTemplate")
    KafkaTemplate<String, String> defaultKafkaTemplate(ProducerFactory<String, String> pf) {
        return new KafkaTemplate<>(pf);
    }


    @Bean
    ProducerFactory<String, Object> dlqProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildProducerProperties());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean("dlqKafkaTemplate")
    KafkaTemplate<String, Object> dlqKafkaTemplate(ProducerFactory<String, Object> pf) {
        return new KafkaTemplate<>(pf);
    }
}

