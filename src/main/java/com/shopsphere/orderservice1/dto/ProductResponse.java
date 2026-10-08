package com.shopsphere.orderservice1.dto;

public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private double price;
    private int quantity;

    public ProductResponse() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}