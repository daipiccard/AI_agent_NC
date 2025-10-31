package com.transacciones.transaction_ingestor.service;

import com.transacciones.transaction_ingestor.dto.TransactionDTO;
import com.transacciones.transaction_ingestor.entity.Transaction;
import com.transacciones.transaction_ingestor.repository.KafkaTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class KafkaTransactionService {

    private static final Logger log = LoggerFactory.getLogger(KafkaTransactionService.class);
    private final KafkaTransactionRepository kafkaTransactionRepository;

    public KafkaTransactionService(KafkaTransactionRepository kafkaTransactionRepository) {
        this.kafkaTransactionRepository = kafkaTransactionRepository;
    }

    @Transactional
    public Transaction saveTransaction(TransactionDTO dto) {
        Transaction transaction = new Transaction();
        
        if (dto.getId() == null || dto.getId().isEmpty()) {
            transaction.setId(UUID.randomUUID().toString());
        } else {
            transaction.setId(dto.getId());
        }
        
        transaction.setAccountId(dto.getAccountId());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType());
        transaction.setDescription(dto.getDescription());
        transaction.setTimestamp(dto.getTimestamp());
        transaction.setStatus(dto.getStatus());
        transaction.setRejectionReason(dto.getRejectionReason());

        Transaction saved = kafkaTransactionRepository.save(transaction);
        log.info("Transacción guardada en BD: {}", saved.getId());
        
        dto.setId(saved.getId());
        return saved;
    }
}