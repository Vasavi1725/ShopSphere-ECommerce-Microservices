
package com.shopsphere.paymentservice.controller;

import com.shopsphere.paymentservice.dto.PaymentStatusRequest;
import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.repository.PaymentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentController paymentController;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment(10L, 799.0, "PENDING", "UPI");
        payment.setId(4L);
    }

    @Test
    void createPaymentSetsPendingStatus() {
        Payment newPayment = new Payment(10L, 799.0, "FAILED", "UPI");

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentController.createPayment(newPayment);

        assertEquals("PENDING", result.getStatus());
        assertEquals(10L, result.getOrderId());
        assertEquals(799.0, result.getAmount());
        verify(paymentRepository).save(newPayment);
    }

    @Test
    void createPaymentRejectsInvalidAmount() {
        Payment invalid = new Payment(10L, 0.0, "PENDING", "UPI");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentController.createPayment(invalid));

        assertEquals(400, exception.getStatusCode().value());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getPaymentByIdReturnsExistingPayment() {
        when(paymentRepository.findById(4L))
                .thenReturn(Optional.of(payment));

        Payment result = paymentController.getPaymentById(4L);

        assertEquals(4L, result.getId());
        assertEquals("PENDING", result.getStatus());
    }

    @Test
    void getPaymentByIdRejectsMissingPayment() {
        when(paymentRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentController.getPaymentById(99L));

        assertEquals(404, exception.getStatusCode().value());
    }

    @Test
    void updatePendingPaymentToSuccess() {
        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setStatus("SUCCESS");

        when(paymentRepository.findById(4L))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payment result = paymentController.updatePaymentStatus(4L, request);

        assertEquals("SUCCESS", result.getStatus());
        verify(paymentRepository).save(payment);
    }

    @Test
    void updateSuccessPaymentToPendingIsRejected() {
        payment.setStatus("SUCCESS");

        PaymentStatusRequest request = new PaymentStatusRequest();
        request.setStatus("PENDING");

        when(paymentRepository.findById(4L))
                .thenReturn(Optional.of(payment));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> paymentController.updatePaymentStatus(4L, request));

        assertEquals(409, exception.getStatusCode().value());
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
