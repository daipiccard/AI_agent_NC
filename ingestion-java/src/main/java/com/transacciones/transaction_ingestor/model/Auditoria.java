package com.transacciones.transaction_ingestor.model;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    /** FK a la alerta */
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_alerta", referencedColumnName = "id_alerta")
    private Alerta alerta;

    /** Acción realizada (por ahora: "auto_creada") */
    @Column(name = "accion", nullable = false, length = 64)
    private String accion = "auto_creada";

    /** Fuente del evento (por ahora: "system") */
    @Column(name = "source", nullable = false, length = 32)
    private String source = "system";

    /** Actor humano (por ahora puede quedar NULL) */
    @Column(name = "actor", length = 64)
    private String actor;

    /** Descripción libre (opcional, se puede usar más adelante) */
    @Column(name = "descripcion_accion", length = 255)
    private String descripcionAccion = "Alerta generada automáticamente por el sistema";

    /** Timestamp del evento */
    @Column(name = "timestamp_accion", nullable = false,
            columnDefinition = "timestamp default current_timestamp")
    private Timestamp timestampAccion;

    // Getters y setters
    public Long getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(Long idAuditoria) { this.idAuditoria = idAuditoria; }

    public Alerta getAlerta() { return alerta; }
    public void setAlerta(Alerta alerta) { this.alerta = alerta; }

    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }

    public String getDescripcionAccion() { return descripcionAccion; }
    public void setDescripcionAccion(String descripcionAccion) { this.descripcionAccion = descripcionAccion; }

    public Timestamp getTimestampAccion() { return timestampAccion; }
    public void setTimestampAccion(Timestamp timestampAccion) { this.timestampAccion = timestampAccion; }
}