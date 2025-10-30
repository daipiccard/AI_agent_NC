package com.transacciones.transaction_ingestor.alerts;

import java.math.BigDecimal;

/** DTO para exponer filas de la tabla alertas */
public record Alert(
        Long idAlerta,          // <— Long en lugar de String
        String idTransaccion,   // <— lo derivamos del entity relacionado
        String idUsuario,
        BigDecimal monto,
        String fecha,
        String hora,
        String bandera,
        String origenFiltro,
        BigDecimal puntuacionFinal,
        String reasonsJson,
        String createdAtIso
) {}