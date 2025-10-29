package com.transacciones.transaction_ingestor.alerts;

import com.transacciones.transaction_ingestor.model.Alerta;
import com.transacciones.transaction_ingestor.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class AlertController {

    private final AlertaRepository alertRepository;

    @Autowired
    public AlertController(AlertaRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @GetMapping("/alerts")
    public List<Alerta> getAlerts() {
        // Elige UNA de estas dos líneas, según lo que implementaste en el repo:
        return alertRepository.findTop100ByOrderByCreatedAtDesc();
        // return alertRepository.findTop100ByOrderByIdAlertaDesc();
    }
}