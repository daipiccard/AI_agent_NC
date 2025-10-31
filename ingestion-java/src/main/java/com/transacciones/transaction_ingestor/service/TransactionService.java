package com.transacciones.transaction_ingestor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transacciones.transaction_ingestor.dto.TransactionDTO;
import com.transacciones.transaction_ingestor.model.Transaccion;
import com.transacciones.transaction_ingestor.model.Usuario;
import com.transacciones.transaction_ingestor.repository.TransactionRepository;
import com.transacciones.transaction_ingestor.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private static final BigDecimal MAX_TRANSACTION_AMOUNT = new BigDecimal("10000.00");


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
        } catch (Exception ignore) {
            /* opcional: loggear */ }

        // 4) Persistir
        return transactionRepository.save(transaction);
    }
    // ========================================
    // NUEVOS MÉTODOS PARA KAFKA
    // ========================================
    
    /**
     * Valida una transacción recibida desde Kafka
     * @param dto TransactionDTO recibido del topic de Kafka
     * @return true si es válida, false si debe rechazarse
     */
    public boolean validateTransaction(TransactionDTO dto) {
        log.debug("Validando transacción desde Kafka: {}", dto);

        // Validación 1: Monto no excede el límite
        if (dto.getAmount() != null && dto.getAmount().compareTo(MAX_TRANSACTION_AMOUNT) > 0) {
            log.warn("Transacción rechazada: monto excede el límite permitido");
            dto.setRejectionReason("Monto excede el límite de " + MAX_TRANSACTION_AMOUNT);
            return false;
        }

        // Validación 2: Tipo de transacción válido
        if (!isValidTransactionType(dto.getType())) {
            log.warn("Transacción rechazada: tipo de transacción inválido");
            dto.setRejectionReason("Tipo de transacción inválido: " + dto.getType());
            return false;
        }

        // Validación 3: Campos obligatorios
        if (dto.getAccountId() == null || dto.getAccountId().isEmpty()) {
            log.warn("Transacción rechazada: ID de cuenta vacío");
            dto.setRejectionReason("ID de cuenta es requerido");
            return false;
        }

        return true;
    }

    /**
     * Convierte TransactionDTO (de Kafka) a Transaccion (entity) y guarda
     * @param dto TransactionDTO validado
     * @return Transaccion guardada
     */
    @Transactional
    public Transaccion saveTransactionFromDTO(TransactionDTO dto) {
        log.info("Guardando transacción desde Kafka: {}", dto);

        // Crear nueva transacción
        Transaccion transaction = new Transaccion();
        
        if (dto.getId() == null || dto.getId().isEmpty()) {
            transaction.setIdTransaccion(UUID.randomUUID().toString());
        } else {
            transaction.setIdTransaccion(dto.getId());
        }

        // Mapear campos del DTO a la entidad
        transaction.setIdUsuario(dto.getAccountId());
        transaction.setMonto(dto.getAmount());
        
        // Usar el método original para guardar (reutilizar lógica existente)
        return ingestAndSave(transaction);
    }

    private boolean isValidTransactionType(String type) {
        return type != null && 
               (type.equals("DEPOSIT") || type.equals("WITHDRAWAL") || type.equals("TRANSFER"));
    }
}
