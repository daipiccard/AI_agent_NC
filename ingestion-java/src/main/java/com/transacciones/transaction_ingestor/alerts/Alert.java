package com.transacciones.transaction_ingestor.alerts;

import java.math.BigDecimal;

/**
 * Simple DTO used to expose alert data to the dashboard.
 */
public record Alert(
        String id,
        BigDecimal monto,
        String fecha,
        String hora,
        String ubicacion,
        String bandera,
        boolean sospechosa
) {
}
