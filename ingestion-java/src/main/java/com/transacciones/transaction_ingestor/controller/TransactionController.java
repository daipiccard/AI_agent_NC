package com.transacciones.transaction_ingestor.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.transacciones.transaction_ingestor.model.Transaction;
import com.transacciones.transaction_ingestor.service.TransactionService;

/**
 * Controlador REST que expone el endpoint para la ingesta de transacciones (Requisito 1).
 */
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Requisito 1: Crear endpoint /transactions/ingest
    @PostMapping("/ingest")
    @ResponseStatus(HttpStatus.CREATED) // Código de respuesta 201
    public String ingestTransaction(
        // @Valid activa las reglas de validación definidas en la clase Transaction (Requisito 2)
        @RequestBody @Valid Transaction transaction
    ) {
        Transaction savedTransaction = transactionService.ingestAndSave(transaction);
        return String.format("Transacción %s guardada exitosamente en la base de datos.", savedTransaction.getIdTransaccion());
    }
}

