package com.transacciones.transaction_ingestor.alerts;

import com.transacciones.transaction_ingestor.model.Transaccion;
import com.transacciones.transaction_ingestor.repository.TransactionRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class AlertController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final TransactionRepository transactionRepository;

    @Autowired
    public AlertController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/alerts")
    public List<Alert> getAlerts() {
        return transactionRepository
                .findTop100ByOrderByFechaDescHoraDesc()
                .stream()
                .map(this::toAlert)
                .toList();
    }

    private Alert toAlert(Transaccion tx) {
        String estado = Optional.ofNullable(tx.getEstado()).orElse("OK");
        boolean sospechosa = estado.toLowerCase(Locale.ROOT).contains("sosp");

        return new Alert(
                tx.getIdTransaccion(),
                tx.getMonto(),
                tx.getFecha() != null ? tx.getFecha().format(DATE_FMT) : "",
                tx.getHora() != null ? tx.getHora().format(TIME_FMT) : "",
                Optional.ofNullable(tx.getUbicacion()).orElse(""),
                estado.toUpperCase(Locale.ROOT),
                sospechosa
        );
    }
}
