package com.transacciones.transaction_ingestor.controller;

import com.transacciones.transaction_ingestor.dto.AlertResponse;
import com.transacciones.transaction_ingestor.service.AlertService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
public class AlertController {

  private final AlertService alertService;

  @Autowired
  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }

  @GetMapping
  public Map<String, List<AlertResponse>> getAlerts() {
    return Map.of("alerts", alertService.getAlerts());
  }
}

