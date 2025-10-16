// filepath: /Users/arielplazasalinas/Documents/NoCountry/AI_agent_NC/ingestion-java/src/main/java/com/transacciones/transaction_ingestor/service/EvaluadorFraude.java
package com.transacciones.transaction_ingestor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.transacciones.transaction_ingestor.model.Transaccion;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

// Se define la clase
@Service
public class EvaluadorFraude {

  private static final String AUDITORIA_FILE = "../dashboard-web/db.json";

  // Metodo principal
  // publico, static no necesita crear un objeto para usarlo
  // String devuelve un texto (estado de la transicion)
  // Transaccionn tx, recibe una instancia del tipo Transaccion como parametro
  public String evaluar(Transaccion tx) {
    // variable booleana evalua si el monto es mayor a 200000 se asigna true
    BigDecimal montoTransaccion = tx.getMonto();
    BigDecimal limite = new BigDecimal(200000);
    boolean sospechosa = montoTransaccion.compareTo(limite) > 0;
    // variable string evalua si sospechosa es true se marca, de lo contrario ok
    String estado = sospechosa ? "sospechosa" : "ok";
    // funcion pasa la transaccion y el estado para ser guardado en un log
    guardarAuditoria(tx, estado);
    // devuelve el estado
    return estado;
  }

  private void guardarAuditoria(Transaccion tx, String estado) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule()); // Registra el módulo JavaTimeModule
    File file = new File(AUDITORIA_FILE);
    List<Map<String, Object>> alerts = new ArrayList<>();

    try {
      // Si el archivo existe y tiene contenido, lee el contenido actual
      if (file.exists() && file.length() > 0) {
        // Lee el archivo como un Map
        Map<String, Object> data = mapper.readValue(file, Map.class);
        // Obtiene la lista de alertas del Map
        alerts = (List<Map<String, Object>>) data.get("alerts");
        if (alerts == null) {
          alerts = new ArrayList<>(); // Si no existe "alerts", crea una nueva lista
        }
      } else {
        // Si el archivo no existe o está vacío, crea una nueva lista
        alerts = new ArrayList<>();
      }

      // Crea un nuevo objeto Map para representar la transacción
      Map<String, Object> transaccionMap = new HashMap<>();
      transaccionMap.put("id", tx.getIdTransaccion());
      transaccionMap.put("monto", tx.getMonto());

      // Formatea la fecha y la hora como strings
      LocalDate fecha = tx.getFecha();
      LocalTime hora = tx.getHora();
      DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
      transaccionMap.put("fecha", fecha.format(dateFormatter));
      transaccionMap.put("hora", hora.format(timeFormatter));

      transaccionMap.put("ubicacion", tx.getUbicacion());
      transaccionMap.put("bandera", estado);

      // Agrega la transacción a la lista de alertas
      alerts.add(transaccionMap);

      // Crea un nuevo Map para envolver la lista de alertas
      Map<String, Object> output = new HashMap<>();
      output.put("alerts", alerts);

      // Escribe la lista de alertas en el archivo JSON
      mapper.writeValue(new File(AUDITORIA_FILE), output);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}