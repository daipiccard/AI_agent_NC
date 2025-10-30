package com.transacciones.transaction_ingestor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transacciones.transaction_ingestor.model.Transaccion;
import com.transacciones.transaction_ingestor.model.Usuario;
import com.transacciones.transaction_ingestor.repository.TransactionRepository;
import com.transacciones.transaction_ingestor.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              UsuarioRepository usuarioRepository,
                              ObjectMapper objectMapper) {
        this.transactionRepository = transactionRepository;
        this.usuarioRepository = usuarioRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Transaccion ingestAndSave(Transaccion transaction) {
        // Log “streaming” (tu salida actual)
        System.out.println("\n---------------------------------------------");
        System.out.printf("🛰️  SIMULACIÓN DE STREAMING DE DATOS RECIBIDA:%n" +
                          "ID: %s | User: %s | Monto: %s | Ubicación: %s%n",
                transaction.getIdTransaccion(),
                transaction.getIdUsuario(),
                transaction.getMonto(),
                (transaction.getLatitud() != null && transaction.getLongitud() != null)
                        ? transaction.getLatitud() + "," + transaction.getLongitud()
                        : "N/A");
        System.out.println("✅  Datos validados y listos para persistencia.");
        System.out.println("---------------------------------------------\n");

        // 1) Resolver/crear Usuario a partir de idUsuario (del JSON)
        String idUsuario = transaction.getIdUsuario();
        if (idUsuario == null || idUsuario.isBlank()) {
            throw new IllegalArgumentException("idUsuario es obligatorio");
        }

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setIdUsuario(idUsuario);
                    // Defaults simples; podés ajustarlos
                    u.setPais(transaction.getPais()); // o "AR" si viene null
                    u.setFechaCreacion(Timestamp.valueOf(LocalDateTime.now()));
                    u.setEstadoCuenta("activa");
                    return usuarioRepository.save(u);
                });

        // 2) Setear la relación en la Transaccion
        transaction.setUsuario(usuario);

        // 3) Guardar el JSON crudo (útil para auditoría)
        try {
            transaction.setRawJson(objectMapper.writeValueAsString(transaction));
        } catch (Exception ignore) { /* opcional: loggear */ }

        // 4) Persistir
        return transactionRepository.save(transaction);
    }
}
