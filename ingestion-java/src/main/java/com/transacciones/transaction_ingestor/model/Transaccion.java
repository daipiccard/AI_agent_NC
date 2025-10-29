package com.transacciones.transaction_ingestor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @Column(name = "id_transaccion", length = 64)
    @NotBlank
    private String idTransaccion;

    // ===== Entrada del JSON (no se persiste). =====
    @Transient
    private String idUsuario;

    // ===== Relación persistida =====
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @NotNull
    private BigDecimal monto;

    @Column(name = "timestamp_transaccion", nullable = false)
    @NotNull
    private LocalDateTime timestampTransaccion;

    @Column(precision = 10, scale = 6, nullable = false)
    private BigDecimal latitud;

    @Column(precision = 10, scale = 6, nullable = false)
    private BigDecimal longitud;

    @Column(length = 4)
    private String pais;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "raw_json", columnDefinition = "json", nullable = false)
    private String rawJson;

    // ===== Getters/Setters =====
    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDateTime getTimestampTransaccion() { return timestampTransaccion; }
    public void setTimestampTransaccion(LocalDateTime t) { this.timestampTransaccion = t; }

    public BigDecimal getLatitud() { return latitud; }
    public void setLatitud(BigDecimal latitud) { this.latitud = latitud; }

    public BigDecimal getLongitud() { return longitud; }
    public void setLongitud(BigDecimal longitud) { this.longitud = longitud; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
}