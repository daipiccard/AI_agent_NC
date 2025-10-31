package com.transacciones.transaction_ingestor.repository;

import com.transacciones.transaction_ingestor.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaccion, String> {

    // Últimas 100 transacciones por fecha/hora descendente
    List<Transaccion> findTop100ByOrderByTimestampTransaccionDesc();

    // Transacciones recientes de un usuario
    List<Transaccion> findByUsuario_IdUsuarioOrderByTimestampTransaccionDesc(String idUsuario);

    // Transacciones en un rango de tiempo
    List<Transaccion> findByTimestampTransaccionBetweenOrderByTimestampTransaccionDesc(
            LocalDateTime desde, LocalDateTime hasta
    );

    // Ejemplo con @Query: mínimo monto + orden por fecha
    @Query("SELECT t FROM Transaccion t WHERE t.monto >= :min ORDER BY t.timestampTransaccion DESC")
    List<Transaccion> findRecentWithMinAmount(@Param("min") BigDecimal min);
    // ========================================
    // NUEVOS MÉTODOS PARA KAFKA - AGREGAR AQUÍ
    // ========================================
    // Buscar por estado (para transacciones validadas/rechazadas)
    // List<Transaccion> findByEstado(String estado);
    
    // // Buscar por usuario y estado
    // List<Transaccion> findByUsuario_IdUsuarioAndEstado(String idUsuario, String estado);

}