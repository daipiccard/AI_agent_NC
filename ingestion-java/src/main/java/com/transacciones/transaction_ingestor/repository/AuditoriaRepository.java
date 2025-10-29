package com.transacciones.transaction_ingestor.repository;

import com.transacciones.transaction_ingestor.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}
