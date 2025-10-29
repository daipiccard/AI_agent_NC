package com.transacciones.transaction_ingestor.model;

import com.transacciones.transaction_ingestor.model.enums.Bandera;
import com.transacciones.transaction_ingestor.model.enums.DecisionSource;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;



@Entity
@Table(
    name = "alertas",
    uniqueConstraints = @UniqueConstraint(name = "uk_alert_tx", columnNames = "id_transaccion"),
    indexes = {
        @Index(name = "idx_alert_created", columnList = "created_at"),
        @Index(name = "idx_alert_flag", columnList = "bandera")
    }
)
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerta")
    private Long idAlerta;

    /** FK a transacciones (idempotencia: una alerta por transacción) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_transaccion", referencedColumnName = "id_transaccion")
    private Transaccion transaccion;

    /** Denormalización útil para consultar rápido en el panel */
    @Column(name = "id_usuario", length = 64)
    private String idUsuario;

    /** Foto del monto en el momento de la decisión */
    @Column(name = "monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    /** Derivados de timestamp_transaccion (evita calcularlos en cada query) */
    @Column(name = "fecha_only", nullable = false)
    private LocalDate fechaOnly;

    @Column(name = "hora_only", nullable = false)
    private LocalTime horaOnly;

    /** ok / review / sospechoso */
    @Enumerated(EnumType.STRING)
    @Column(name = "bandera", nullable = false, length = 12)
    private Bandera bandera;

    /**
     * Puntaje final:
     * - 1.0 si una regla eliminatoria se violó (bloqueo inmediato).
     * - score del modelo en el camino normal.
     */
    @Column(name = "puntuacion_final", precision = 5, scale = 4)
    private BigDecimal puntuacionFinal;

    /** rule | model | combined | analyst | system */
    @Enumerated(EnumType.STRING)
    @Column(name = "origen_filtro", length = 16)
    private DecisionSource origenFiltro;

    /** Motivos estructurados (opcional). Ej: ["monto>200000","pais_no_habitual"] */
    @Column(name = "reasons_json", columnDefinition = "json")
    private String reasonsJson;

    @Column(name = "created_at", nullable = false,
            columnDefinition = "timestamp default current_timestamp")
    private java.sql.Timestamp createdAt;

    /* ================== Getters / Setters ================== */

    public Long getIdAlerta() { return idAlerta; }
    public void setIdAlerta(Long idAlerta) { this.idAlerta = idAlerta; }

    public Transaccion getTransaccion() { return transaccion; }
    public void setTransaccion(Transaccion transaccion) { this.transaccion = transaccion; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getFechaOnly() { return fechaOnly; }
    public void setFechaOnly(LocalDate fechaOnly) { this.fechaOnly = fechaOnly; }

    public LocalTime getHoraOnly() { return horaOnly; }
    public void setHoraOnly(LocalTime horaOnly) { this.horaOnly = horaOnly; }

    public Bandera getBandera() { return bandera; }
    public void setBandera(Bandera bandera) { this.bandera = bandera; }

    public BigDecimal getPuntuacionFinal() { return puntuacionFinal; }
    public void setPuntuacionFinal(BigDecimal puntuacionFinal) { this.puntuacionFinal = puntuacionFinal; }

    public DecisionSource getOrigenFiltro() { return origenFiltro; }
    public void setOrigenFiltro(DecisionSource origenFiltro) { this.origenFiltro = origenFiltro; }

    public String getReasonsJson() { return reasonsJson; }
    public void setReasonsJson(String reasonsJson) { this.reasonsJson = reasonsJson; }

    public java.sql.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.sql.Timestamp createdAt) { this.createdAt = createdAt; }
}