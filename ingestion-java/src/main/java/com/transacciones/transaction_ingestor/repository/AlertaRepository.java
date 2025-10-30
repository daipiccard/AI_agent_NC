package com.transacciones.transaction_ingestor.repository;

import com.transacciones.transaction_ingestor.model.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    // Usa el campo que tengas disponible para ordenar (elige UNA de estas dos firmas)
    List<Alerta> findTop100ByOrderByCreatedAtDesc();   // si tu entidad tiene createdAt

    // Si NO tenés createdAt, usá el ID (ajusta el nombre exacto del campo):
    // List<Alerta> findTop100ByOrderByIdAlertaDesc();
}