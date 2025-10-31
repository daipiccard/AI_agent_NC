package com.transacciones.transaction_ingestor.kafka;

import com.transacciones.transaction_ingestor.dto.TransactionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TransactionProducer {

    private static final Logger log = LoggerFactory.getLogger(TransactionProducer.class);

    private final KafkaTemplate<String, TransactionDTO> kafkaTemplate;

    @Value("${kafka.topics.transaction-input}")
    private String transactionInputTopic;

    @Value("${kafka.topics.transaction-validated}")
    private String transactionValidatedTopic;

    @Value("${kafka.topics.transaction-rejected}")
    private String transactionRejectedTopic;

    public TransactionProducer(KafkaTemplate<String, TransactionDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(TransactionDTO transaction) {
        log.info("Enviando transacción al topic {}: {}", transactionInputTopic, transaction);
        
        CompletableFuture<SendResult<String, TransactionDTO>> future = 
            kafkaTemplate.send(transactionInputTopic, transaction.getAccountId(), transaction);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Transacción enviada exitosamente: [{}] con offset: [{}]", 
                    transaction.getId(), 
                    result.getRecordMetadata().offset());
            } else {
                log.error("Error al enviar transacción: [{}]", transaction.getId(), ex);
            }
        });
    }

    public void sendValidatedTransaction(TransactionDTO transaction) {
        log.info("Enviando transacción validada al topic {}: {}", transactionValidatedTopic, transaction);
        kafkaTemplate.send(transactionValidatedTopic, transaction.getAccountId(), transaction);
    }

    public void sendRejectedTransaction(TransactionDTO transaction) {
        log.info("Enviando transacción rechazada al topic {}: {}", transactionRejectedTopic, transaction);
        kafkaTemplate.send(transactionRejectedTopic, transaction.getAccountId(), transaction);
    }
}