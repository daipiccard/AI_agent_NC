package com.transacciones.transaction_ingestor.dto;

import com.transacciones.transaction_ingestor.model.Transaccion;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Representa la vista resumida de una transacción que se expone como alerta en el dashboard.
 */
public record AlertResponse(
    String id,
    BigDecimal monto,
    LocalDate fecha,
    LocalTime hora,
    String ubicacion,
    String bandera
) {

  public static AlertResponse fromTransaccion(Transaccion transaccion) {
    return new AlertResponse(
        transaccion.getIdTransaccion(),
        transaccion.getMonto(),
        transaccion.getFecha(),
        transaccion.getHora(),
        transaccion.getUbicacion(),
        transaccion.getEstado());
  }
}

