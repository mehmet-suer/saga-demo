package com.saga.streamer.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KafkaTopicProperties.class)
public class KafkaTopicConfig {
    @Bean(name = "kafkaTopicProperties")
    public KafkaTopicProperties kafkaTopicProperties(KafkaTopicProperties props) {
        return props;
    }
}

