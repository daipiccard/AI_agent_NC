package com.transacciones.transaction_ingestor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.transacciones.transaction_ingestor.kafka.TransactionProducer;
import com.transacciones.transaction_ingestor.model.Transaccion;
import com.transacciones.transaction_ingestor.repository.TransactionRepository;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionProducer transactionProducer;

    @Autowired
    public TransactionService(
        TransactionRepository transactionRepository,
        TransactionProducer transactionProducer
    ) {
        this.transactionRepository = transactionRepository;
        this.transactionProducer = transactionProducer;
    }

    /**
     * Procesa la transacción: la envía a Kafka (streaming real)
     */
    public Transaccion ingestAndSave(Transaccion transaction) {
        System.out.println("-----------------------------------------------------------------------");
        System.out.println("🎯 NUEVA TRANSACCIÓN RECIBIDA VIA API:");
        System.out.printf("  ID: %s | User: %s | Monto: %s | Ubicación: %s%n",
                          transaction.getIdTransaccion(),
                          transaction.getUserId(),
                          transaction.getMonto(),
                          transaction.getUbicacion());
        System.out.println("  ➡️  Enviando a Kafka para procesamiento asíncrono...");
        System.out.println("-----------------------------------------------------------------------");

        // Enviar a Kafka en lugar de guardar directamente
        transactionProducer.sendTransaction(transaction);
        
        return transaction;
    }
}