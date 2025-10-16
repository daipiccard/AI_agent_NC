package com.transacciones.transaction_ingestor.service;

import com.transacciones.transaction_ingestor.dto.AlertResponse;
import com.transacciones.transaction_ingestor.repository.TransactionRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

  private final TransactionRepository transactionRepository;

  @Autowired
  public AlertService(TransactionRepository transactionRepository) {
    this.transactionRepository = transactionRepository;
  }

  public List<AlertResponse> getAlerts() {
    return transactionRepository
        .findAll(Sort.by(Sort.Direction.DESC, "fecha", "hora"))
        .stream()
        .map(AlertResponse::fromTransaccion)
        .toList();
  }
}

