package com.transacciones.transaction_ingestor.alerts;

import com.transacciones.transaction_ingestor.model.Alerta;
import com.transacciones.transaction_ingestor.repository.AlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.transacciones.transaction_ingestor.model.Transaccion;
import java.util.Optional;
import java.util.stream.Collectors;
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
   public List<Alert> getAlerts() {
        return alertRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


private Alert mapToDto(Alerta alerta) {
        String id = Optional.ofNullable(alerta.getTransaccion())
                .map(Transaccion::getIdTransaccion)
                .filter(s -> !s.isBlank())
                .orElseGet(() -> Optional.ofNullable(alerta.getIdAlerta())
                        .map(String::valueOf)
                        .orElse(""));

        String fecha = Optional.ofNullable(alerta.getFechaOnly())
                .map(Object::toString)
                .orElse("");

        String hora = Optional.ofNullable(alerta.getHoraOnly())
                .map(Object::toString)
                .orElse("");

        Transaccion tx = alerta.getTransaccion();
        String pais = Optional.ofNullable(tx)
                .map(Transaccion::getPais)
                .filter(p -> !p.isBlank())
                .orElse("-");

        String ubicacion;
        if (tx != null && tx.getLatitud() != null && tx.getLongitud() != null) {
            ubicacion = String.format("%s (%.4f, %.4f)",
                    pais,
                    tx.getLatitud().doubleValue(),
                    tx.getLongitud().doubleValue());
        } else {
            ubicacion = pais;
        }

        String bandera = Optional.ofNullable(alerta.getBandera())
                .map(Enum::name)
                .orElse("ok");

        boolean sospechosa = !"ok".equalsIgnoreCase(bandera);

        return new Alert(
                id,
                alerta.getMonto(),
                fecha,
                hora,
                ubicacion,
                bandera,
                sospechosa
        );
    }
}