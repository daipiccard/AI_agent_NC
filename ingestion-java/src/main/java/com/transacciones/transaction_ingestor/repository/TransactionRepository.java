package com.transacciones.transaction_ingestor.repository;

import com.transacciones.transaction_ingestor.model.Transaccion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Interfaz para la persistencia de datos (Requisito 3).
 * Extiende JpaRepository para obtener métodos CRUD automáticamente.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaccion, String> {
    // String es el tipo de la clave primaria (idTransaccion)

    List<Transaccion> findTop100ByOrderByFechaDescHoraDesc();
}
