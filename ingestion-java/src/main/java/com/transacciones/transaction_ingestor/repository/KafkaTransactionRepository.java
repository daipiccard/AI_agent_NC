package com.transacciones.transaction_ingestor.repository;

import com.transacciones.transaction_ingestor.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KafkaTransactionRepository extends JpaRepository<Transaction, String> {
    
    List<Transaction> findByAccountId(String accountId);
    
    List<Transaction> findByStatus(String status);
    
    List<Transaction> findByAccountIdAndStatus(String accountId, String status);
}