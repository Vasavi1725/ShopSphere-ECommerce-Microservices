package com.shopsphere.paymentservice.controller;

import com.shopsphere.paymentservice.dto.PaymentStatusRequest;
import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.repository.PaymentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

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

    @PatchMapping("/{id}/status")
    public Payment updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody PaymentStatusRequest request) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        if (request.getStatus() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required");
        }

        String newStatus = request.getStatus()
                .trim().toUpperCase(Locale.ROOT);

        if (!List.of("PENDING", "SUCCESS", "FAILED")
                .contains(newStatus)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be PENDING, SUCCESS, or FAILED");
        }

        if (!"PENDING".equals(payment.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending payments can be updated");
        }

        payment.setStatus(newStatus);
        return paymentRepository.save(payment);
    }
}
