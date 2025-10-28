package com.transacciones.transaction_ingestor.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad JPA que representa la tabla 'transaction' en la base de datos.
 * Contiene las reglas de validación para los datos entrantes (Requisito 2).
 */
@Entity
public class Transaccion {

    @Id
    @NotBlank(message = "El ID de la transacción es obligatorio.")
    private String idTransaccion;

    @NotBlank(message = "El ID de usuario es obligatorio.")
    private String userId;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser positivo.")
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotBlank(message = "La ubicación es obligatoria.")
    private String ubicacion;

    // Estado/flag que puede contener valores como "OK", "SOSP" u otros indicadores
    private String estado;

    // Getters y Setters (necesarios para JPA y JSON/de-serialization)

    public String getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(String idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

