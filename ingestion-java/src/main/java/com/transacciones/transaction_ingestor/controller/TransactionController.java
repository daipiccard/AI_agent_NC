package com.transacciones.transaction_ingestor.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transacciones.transaction_ingestor.dto.TransactionDTO;
import com.transacciones.transaction_ingestor.kafka.TransactionProducer;
import com.transacciones.transaction_ingestor.model.Transaccion;
import com.transacciones.transaction_ingestor.service.TransactionService;

import java.util.UUID;

/**
 * Controlador REST que expone el endpoint para la ingesta de transacciones (Requisito 1).
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    // ========================================
    // DECLARACIÓN DE SERVICIOS
    // ========================================
    private final TransactionService transactionService;
    private final TransactionProducer transactionProducer;

    @Autowired
    public TransactionController(TransactionService transactionService,
                                  TransactionProducer transactionProducer) {
        this.transactionService = transactionService;
        this.transactionProducer = transactionProducer;
    }

    // ========================================
    // ENDPOINT ORIGINAL
    // ========================================
    // Requisito 1: Crear endpoint /transactions/ingest
    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.CREATED) // Código de respuesta 201
    public String ingestTransaction(
            // @Valid activa las reglas de validación definidas en la clase Transaction (Requisito 2)
            @RequestBody @Valid Transaccion transaction) {
        Transaccion savedTransaction = transactionService.ingestAndSave(transaction);
        return String.format("Transacción %s guardada exitosamente en la base de datos.",
                savedTransaction.getIdTransaccion());
    }

    // ========================================
    // NUEVOS ENDPOINTS PARA KAFKA
    // ========================================
    /**
     * Endpoint para enviar transacciones a Kafka
     */
    @PostMapping("/kafka")
    public ResponseEntity<String> sendToKafka(@Valid @RequestBody TransactionDTO transaction) {
        // Generar ID si no viene
        if (transaction.getId() == null || transaction.getId().isEmpty()) {
            transaction.setId(UUID.randomUUID().toString());
        }

        // Enviar a Kafka
        transactionProducer.sendTransaction(transaction);

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body("Transacción enviada a Kafka para procesamiento: " + transaction.getId());
    }
}