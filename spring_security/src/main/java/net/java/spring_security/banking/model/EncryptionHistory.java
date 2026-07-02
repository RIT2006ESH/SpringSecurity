package net.java.spring_security.banking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "encryption_history")
public class EncryptionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_id")
    private Integer customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type")
    private OperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType;

    @Column(name = "performed_at")
    private LocalDateTime performedAt;

    @Column(name = "performed_by")
    private String performedBy;

    public enum OperationType { ENCRYPT, DECRYPT }
    public enum DataType { AADHAAR, PAN }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId; }

    public OperationType getOperationType() { return operationType; }
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType; }

    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }

    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) {
        this.performedAt = performedAt; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy; }
}