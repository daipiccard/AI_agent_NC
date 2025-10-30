package com.transacciones.transaction_ingestor.alerts;

import com.transacciones.transaction_ingestor.model.Alerta;
import com.transacciones.transaction_ingestor.model.enums.Bandera;
import com.transacciones.transaction_ingestor.model.enums.DecisionSource;
import com.transacciones.transaction_ingestor.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
@CrossOrigin(origins = "*")
public class AlertController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AlertaRepository alertaRepository;

    @Autowired
    public AlertController(AlertaRepository alertaRepository) {
        this.alertaRepository = alertaRepository;
    }

    @GetMapping("/alerts")
    public List<Alert> getAlerts() {
        return alertaRepository
                .findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDto)
                .toList();
    }

   private Alert toDto(com.transacciones.transaction_ingestor.model.Alerta a) {
    // fecha/hora como ya tenías
    String fecha = (a.getFechaOnly() != null) ? a.getFechaOnly().format(DATE_FMT) : "";
    String hora  = (a.getHoraOnly()  != null) ? a.getHoraOnly().format(TIME_FMT) : "";

    String createdAtIso = null;
    if (a.getCreatedAt() != null) {
        createdAtIso = a.getCreatedAt().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .toString();
    }

    // id de transacción: si es relación ManyToOne, sacamos el string del entity
    String idTx = null;
    if (a.getIdTransaccion() != null) { // su getter devuelve Transaccion
        idTx = a.getIdTransaccion().getIdTransaccion();
    }

    String bandera = (a.getBandera() != null) ? a.getBandera().name() : null;
    String origen  = (a.getOrigenFiltro() != null) ? a.getOrigenFiltro().name().toLowerCase() : null;

    return new Alert(
            a.getIdAlerta(),          // Long
            idTx,                     // String id de Transaccion
            a.getIdUsuario(),
            a.getMonto(),
            fecha,
            hora,
            bandera,
            origen,
            a.getPuntuacionFinal(),
            a.getReasonsJson(),
            createdAtIso
    );
}
}