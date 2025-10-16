package com.transacciones.transaction_ingestor.alerts;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class AlertController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final List<Alert> demoAlerts = List.of(
            new Alert(
                    "TX-0001",
                    new BigDecimal("152300"),
                    LocalDate.now().minusDays(1).format(DATE_FMT),
                    LocalTime.now().minusMinutes(15).format(TIME_FMT),
                    "Buenos Aires",
                    "SOSPECHOSA",
                    true
            ),
            new Alert(
                    "TX-0002",
                    new BigDecimal("78000"),
                    LocalDate.now().minusDays(2).format(DATE_FMT),
                    LocalTime.now().minusHours(2).format(TIME_FMT),
                    "Córdoba",
                    "REVIEW",
                    true
            ),
            new Alert(
                    "TX-0003",
                    new BigDecimal("20500"),
                    LocalDate.now().format(DATE_FMT),
                    LocalTime.now().minusMinutes(5).format(TIME_FMT),
                    "Mendoza",
                    "OK",
                    false
            )
    );

    @GetMapping("/alerts")
    public List<Alert> getAlerts() {
        return demoAlerts;
    }
}
