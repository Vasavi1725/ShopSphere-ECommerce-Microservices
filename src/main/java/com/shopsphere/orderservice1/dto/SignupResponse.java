package com.shopsphere.orderservice1.dto;

public record SignupResponse(
    Long id,
    String email,
    String message
) {
}
