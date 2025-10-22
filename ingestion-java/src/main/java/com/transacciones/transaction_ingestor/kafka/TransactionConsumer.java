package com.transacciones.transaction_ingestor.kafka;

import com.transacciones.transaction_ingestor.model.Transaccion;
import com.transacciones.transaction_ingestor.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionConsumer(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Escucha mensajes del topic de transacciones y los guarda en la base de datos
     */
    @KafkaListener(
        topics = "${app.kafka.topic.transactions}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consumeTransaction(Transaccion transaction) {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("📥 TRANSACCIÓN RECIBIDA DESDE KAFKA:");
        System.out.printf("  ID: %s | User: %s | Monto: %s | Ubicación: %s%n",
                          transaction.getIdTransaccion(),
                          transaction.getUserId(),
                          transaction.getMonto(),
                          transaction.getUbicacion());
        
        // Guardar en la base de datos
        transactionRepository.save(transaction);
        
        System.out.println("  ✅ Transacción guardada en MySQL");
        System.out.println("-----------------------------------------------------------------------");
    }
}