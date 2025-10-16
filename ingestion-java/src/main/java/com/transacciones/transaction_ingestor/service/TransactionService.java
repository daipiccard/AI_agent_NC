package com.transacciones.transaction_ingestor.service;

import com.transacciones.transaction_ingestor.model.Transaccion;
import com.transacciones.transaction_ingestor.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Contiene la lógica de negocio. Implementa la simulación de streaming (Requisito 4) y llama al
 * repositorio para guardar.
 */
@Service
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final EvaluadorFraude evaluadorFraude;

  @Autowired
  public TransactionService(
      TransactionRepository transactionRepository, EvaluadorFraude evaluadorFraude) {
    this.transactionRepository = transactionRepository;
    this.evaluadorFraude = evaluadorFraude;
  }

  /** Procesa la transacción: simula la ingesta en streaming y guarda en la DB. */
  public Transaccion ingestAndSave(Transaccion transaction) {
    // --- Punto 4: Simulación de "Streaming" ---
    System.out.println(
        "-----------------------------------------------------------------------");
    System.out.println("🤖 SIMULACIÓN DE STREAMING DE DATOS RECIBIDA:");
    System.out.printf(
        "  ID: %s | User: %s | Monto: %s | Ubicación: %s%n",
        transaction.getIdTransaccion(),
        transaction.getUserId(),
        transaction.getMonto(),
        transaction.getUbicacion());
    System.out.println("  Datos validados y listos para persistencia.");
    System.out.println(
        "-----------------------------------------------------------------------");

    // --- Integración del Evaluador de Fraude ---
    String estado = evaluadorFraude.evaluar(transaction);
    transaction.setEstado(estado);

    // --- Punto 3: Guardar la transacción en MySQL (JPA) ---
    return transactionRepository.save(transaction);
  }
}