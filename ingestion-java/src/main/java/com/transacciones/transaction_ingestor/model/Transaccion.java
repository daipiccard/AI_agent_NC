package com.transacciones.transaction_ingestor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entidad JPA que representa la tabla 'transacciones' en la base de datos.
 * Está vinculada con las tablas 'usuarios' y 'alertas'.
 */
@Entity
@Table(name = "transacciones")
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTransaccion; // Cambiado a autoincremental para relación con otras tablas

    // Relación: muchas transacciones pertenecen a un usuario
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @NotNull(message = "El monto es obligatorio.")
    @DecimalMin(value = "0.01", inclusive = true, message = "El monto debe ser positivo.")
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria.")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria.")
    private LocalTime hora;

    @NotNull(message = "La ubicación es obligatoria.")
    private String ubicacion;

    // Estado o flag (por ejemplo "OK", "SOSP", etc.)
    private String estado;

    // Relación uno a uno con la tabla alertas (una transacción puede tener una alerta)
    @OneToOne(mappedBy = "transaccion", cascade = CascadeType.ALL)
    private Alerta alerta;

    // ----- Getters y Setters -----

    public Long getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(Long idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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
}