
package com.shopsphere.paymentservice.controller;

import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.repository.PaymentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment createPayment(@RequestBody Payment payment) {

        if (payment.getOrderId() == null
                || payment.getOrderId() <= 0
                || !Double.isFinite(payment.getAmount())
                || payment.getAmount() <= 0
                || payment.getPaymentMethod() == null
                || payment.getPaymentMethod().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment details");
        }

        payment.setId(null);
        payment.setStatus("PENDING");

        return paymentRepository.save(payment);
    }
}