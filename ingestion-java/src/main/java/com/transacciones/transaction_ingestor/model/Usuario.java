package com.transacciones.transaction_ingestor.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @Column(name = "id_usuario", length = 64)
    @NotBlank
    private String idUsuario;

    @Column(name = "pais", length = 2)
    private String pais; // AR, BR...

    @Column(name = "estado_cuenta", length = 20)
    private String estadoCuenta = "activa"; // activa/suspendida/cerrada

    @Column(name = "fecha_creacion", nullable = false,
            columnDefinition = "timestamp default current_timestamp")
    private java.sql.Timestamp fechaCreacion;

    /* getters/setters */
    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(String estadoCuenta) { this.estadoCuenta = estadoCuenta; }
    public java.sql.Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(java.sql.Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
