// filepath: /Users/arielplazasalinas/Documents/NoCountry/AI_agent_NC/ingestion-java/src/main/java/com/transacciones/transaction_ingestor/service/EvaluadorFraude.java
package com.transacciones.transaction_ingestor.service;

import com.transacciones.transaction_ingestor.model.Transaccion;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

// Se define la clase
@Service
public class EvaluadorFraude {

  // Metodo principal
  // publico, static no necesita crear un objeto para usarlo
  // String devuelve un texto (estado de la transicion)
  // Transaccionn tx, recibe una instancia del tipo Transaccion como parametro
  public String evaluar(Transaccion tx) {
    // variable booleana evalua si el monto es mayor a 200000 se asigna true
    BigDecimal montoTransaccion = tx.getMonto();
    BigDecimal limite = BigDecimal.valueOf(200_000L);
    boolean sospechosa = montoTransaccion.compareTo(limite) > 0;
    // variable string evalua si sospechosa es true se marca, de lo contrario ok
    return sospechosa ? "sospechosa" : "ok";
  }
}