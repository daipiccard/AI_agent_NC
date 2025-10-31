package com.transacciones.transaction_ingestor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    private String id;

    @NotBlank(message = "El ID de cuenta no puede estar vacío")
    private String accountId;

    @NotNull(message = "El monto es requerido")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;

    @NotBlank(message = "El tipo de transacción es requerido")
    private String type; // DEPOSIT, WITHDRAWAL, TRANSFER

    @NotBlank(message = "La descripción es requerida")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private String status; // PENDING, VALIDATED, REJECTED

    private String rejectionReason;

    // Constructors
    public TransactionDTO() {
        this.timestamp = LocalDateTime.now();
        this.status = "PENDING";
    }

    public TransactionDTO(String accountId, BigDecimal amount, String type, String description) {
        this();
        this.accountId = accountId;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return "TransactionDTO{" +
                "id='" + id + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}