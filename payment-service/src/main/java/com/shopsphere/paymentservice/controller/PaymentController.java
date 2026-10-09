
package com.shopsphere.paymentservice.controller;

import com.shopsphere.paymentservice.entity.Payment;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final List<Payment> payments = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @GetMapping
    public List<Payment> getAllPayments() {
        return payments;
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return payments.stream()
                .filter(payment -> payment.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment createPayment(@RequestBody Payment payment) {

        if (payment.getOrderId() == null
                || payment.getOrderId() <= 0
                || payment.getAmount() <= 0
                || payment.getPaymentMethod() == null
                || payment.getPaymentMethod().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment details");
        }

        payment.setId(idGenerator.getAndIncrement());
        payment.setStatus("PENDING");

        payments.add(payment);

        return payment;
    }
}