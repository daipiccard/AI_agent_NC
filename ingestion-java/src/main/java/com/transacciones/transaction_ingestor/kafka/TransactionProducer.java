package com.transacciones.transaction_ingestor.kafka;

import com.transacciones.transaction_ingestor.model.Transaccion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TransactionProducer {

    @Value("${app.kafka.topic.transactions}")
    private String transactionsTopic;

    private final KafkaTemplate<String, Transaccion> kafkaTemplate;

    @Autowired
    public TransactionProducer(KafkaTemplate<String, Transaccion> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Envía una transacción al topic de Kafka
     */
    public void sendTransaction(Transaccion transaction) {
        System.out.println("📤 Enviando transacción a Kafka: " + transaction.getIdTransaccion());
        
        CompletableFuture<SendResult<String, Transaccion>> future = 
            kafkaTemplate.send(transactionsTopic, transaction.getIdTransaccion(), transaction);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("✅ Transacción enviada exitosamente a Kafka: " + 
                    transaction.getIdTransaccion() + 
                    " | Offset: " + result.getRecordMetadata().offset());
            } else {
                System.err.println("❌ Error al enviar transacción a Kafka: " + ex.getMessage());
            }
        });
    }
}