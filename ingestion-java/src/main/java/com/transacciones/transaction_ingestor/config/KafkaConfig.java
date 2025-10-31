package com.transacciones.transaction_ingestor.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.transaction-input}")
    private String transactionInputTopic;

    @Value("${kafka.topics.transaction-validated}")
    private String transactionValidatedTopic;

    @Value("${kafka.topics.transaction-rejected}")
    private String transactionRejectedTopic;

    @Bean
    public NewTopic transactionInputTopic() {
        return TopicBuilder.name(transactionInputTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionValidatedTopic() {
        return TopicBuilder.name(transactionValidatedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionRejectedTopic() {
        return TopicBuilder.name(transactionRejectedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}