package com.shopsphere.notificationservice;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String recipient;
    private String type;
    private String message;
    private String status = "CREATED";
    private Instant createdAt = Instant.now();

    protected Notification() {
    }

    public Notification(Long orderId, String recipient, String type, String message) {
        this.orderId = orderId;
        this.recipient = recipient;
        this.type = type;
        this.message = message;
        this.status = "CREATED";
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public String getRecipient() { return recipient; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
