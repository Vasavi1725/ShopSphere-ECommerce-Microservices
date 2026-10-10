package com.shopsphere.orderservice1.dto;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private double amount;
    private String status;
    private String paymentMethod;

    public PaymentResponse() {
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
