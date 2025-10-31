package com.transacciones.transaction_ingestor.kafka;

import com.transacciones.transaction_ingestor.dto.TransactionDTO;
import com.transacciones.transaction_ingestor.service.KafkaTransactionService;
import com.transacciones.transaction_ingestor.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class TransactionConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransactionConsumer.class);

    private final TransactionService transactionService;
    private final KafkaTransactionService kafkaTransactionService;
    private final TransactionProducer transactionProducer;

    public TransactionConsumer(TransactionService transactionService,
                               KafkaTransactionService kafkaTransactionService,
                               TransactionProducer transactionProducer) {
        this.transactionService = transactionService;
        this.kafkaTransactionService = kafkaTransactionService;
        this.transactionProducer = transactionProducer;
    }

    @KafkaListener(
        topics = "${kafka.topics.transaction-input}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransaction(
            @Payload TransactionDTO transaction,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Recibida transacción de partition: {} offset: {} - {}", partition, offset, transaction);

        try {
            // Validar la transacción
            boolean isValid = transactionService.validateTransaction(transaction);

            if (isValid) {
                transaction.setStatus("VALIDATED");
                kafkaTransactionService.saveTransaction(transaction);
                transactionProducer.sendValidatedTransaction(transaction);
                log.info("Transacción validada y guardada: {}", transaction.getId());
            } else {
                transaction.setStatus("REJECTED");
                transactionProducer.sendRejectedTransaction(transaction);
                log.warn("Transacción rechazada: {}", transaction.getId());
            }

            // Confirmar el procesamiento del mensaje
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error procesando transacción: {}", transaction, e);
            // En caso de error, no hacemos acknowledge para que se reintente
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.transaction-validated}",
        groupId = "${spring.kafka.consumer.group-id}-validated"
    )
    public void consumeValidatedTransaction(@Payload TransactionDTO transaction) {
        log.info("Procesando transacción validada: {}", transaction);
    }

    @KafkaListener(
        topics = "${kafka.topics.transaction-rejected}",
        groupId = "${spring.kafka.consumer.group-id}-rejected"
    )
    public void consumeRejectedTransaction(@Payload TransactionDTO transaction) {
        log.info("Procesando transacción rechazada: {}", transaction);
    }
}