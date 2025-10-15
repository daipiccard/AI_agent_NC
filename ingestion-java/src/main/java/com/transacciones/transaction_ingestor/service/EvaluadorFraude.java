package com.transacciones.transaction_ingestor.service;

import com.transacciones.transaction_ingestor.model.Transaccion;
import java.io.FileWriter;
import java.io.IOException;
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
    // creacion deobjeto Filewriter para abrir o crear nuevo archivo, true nuevos archivos se agregan al
    // final
    try (FileWriter fw = new FileWriter("../dashboard-web/auditoria.json", true)) {
      // linea que se guarda en el archivo
      fw.write(tx.getIdTransaccion() + " - " + estado + " - $" + tx.getMonto() + "\n");
      // maneja excepcones
    } catch (IOException e) {
      // imprime el error completo por consola
      e.printStackTrace();
    }
  }
}