package com.shopsphere.orderservice1.service;

import com.shopsphere.orderservice1.event.OrderEventPublisher;

import com.shopsphere.orderservice1.dto.CreateOrderRequest;
import com.shopsphere.orderservice1.dto.PaymentResponse;
import com.shopsphere.orderservice1.dto.ProductResponse;
import com.shopsphere.orderservice1.entity.Order;
import com.shopsphere.orderservice1.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestClient productRestClient;
    private final RestClient paymentRestClient;
    private final RestClient notificationRestClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            @Qualifier("productRestClient") RestClient productRestClient,
            @Qualifier("paymentRestClient") RestClient paymentRestClient,
            @Qualifier("notificationRestClient") RestClient notificationRestClient,
            OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.productRestClient = productRestClient;
        this.paymentRestClient = paymentRestClient;
        this.notificationRestClient = notificationRestClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(CreateOrderRequest request) {

        if (request == null || request.getQuantity() <= 0) {
            return null;
        }

        // Step 1: Fetch product details
        ProductResponse product = productRestClient.get()
                .uri("/products/" + request.getProductId())
                .retrieve()
                .body(ProductResponse.class);

        if (product == null) {
            return null;
        }

        // Step 2: Check available stock
        if (product.getQuantity() < request.getQuantity()) {
            return null;
        }

        // Step 3: Create and save the order
        Order order = new Order(
                request.getProductId(),
                product.getName(),
                request.getQuantity(),
                product.getPrice()
        );

        order.setOrderStatus("PAYMENT_PENDING");
        Order savedOrder = orderRepository.save(order);

        // Step 4: Reduce stock
        ProductResponse reducedProduct;

        try {
            reducedProduct = productRestClient.put()
                    .uri("/products/"
                            + request.getProductId()
                            + "/reduce-stock?quantity="
                            + request.getQuantity())
                    .retrieve()
                    .body(ProductResponse.class);

        } catch (RuntimeException exception) {
            savedOrder.setOrderStatus("PAYMENT_FAILED");
            return orderRepository.save(savedOrder);
        }

        if (reducedProduct == null) {
            savedOrder.setOrderStatus("PAYMENT_FAILED");
            return orderRepository.save(savedOrder);
        }

        // Step 5: Create the payment record
        Map<String, Object> paymentRequest = Map.of(
                "orderId", savedOrder.getId(),
                "amount", savedOrder.getPrice() * savedOrder.getQuantity(),
                "paymentMethod", "UPI"
        );

        try {
            PaymentResponse paymentResponse = paymentRestClient.post()
                    .uri("/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paymentRequest)
                    .retrieve()
                    .body(PaymentResponse.class);

            if (paymentResponse == null) {
                throw new IllegalStateException(
                        "Payment Service returned an empty response"
                );
            }

            // Payment is pending until the payment service reports SUCCESS.
            return savedOrder;

        } catch (RuntimeException exception) {

            // Step 6: Payment creation failed; restore stock.
            try {
                ProductResponse restoredProduct = productRestClient.put()
                        .uri("/products/"
                                + request.getProductId()
                                + "/increase-stock?quantity="
                                + request.getQuantity())
                        .retrieve()
                        .body(ProductResponse.class);

                if (restoredProduct == null) {
                    throw new IllegalStateException(
                            "Stock restoration returned an empty response"
                    );
                }

                savedOrder.setOrderStatus("PAYMENT_FAILED");
                return orderRepository.save(savedOrder);

            } catch (RuntimeException restorationException) {

                savedOrder.setOrderStatus("PAYMENT_FAILED");
                orderRepository.save(savedOrder);

                throw new IllegalStateException(
                        "Payment creation failed, and stock restoration "
                                + "could not be confirmed. Check product stock.",
                        restorationException
                );
            }
        }
    }

    // Verify payment before confirming the order.
    public Order confirmOrder(Long orderId) {

        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            return null;
        }

        // Prevent an already-confirmed order from being confirmed again.
        if ("CONFIRMED".equals(order.getOrderStatus())) {
            return null;
        }

        // Only pending orders can be confirmed.
        if (!"PAYMENT_PENDING".equals(order.getOrderStatus())) {
            return null;
        }

        PaymentResponse[] payments = paymentRestClient.get()
                .uri("/payments")
                .retrieve()
                .body(PaymentResponse[].class);

        if (payments == null) {
            return null;
        }

        double expectedAmount =
                order.getPrice() * order.getQuantity();

        boolean paymentSuccessful = false;

        for (PaymentResponse payment : payments) {
            if (payment != null
                    && payment.getOrderId() != null
                    && payment.getOrderId().equals(orderId)
                    && "SUCCESS".equalsIgnoreCase(payment.getStatus())
                    && Double.compare(
                    payment.getAmount(),
                    expectedAmount
            ) == 0) {

                paymentSuccessful = true;
                break;
            }
        }

        if (!paymentSuccessful) {
            return null;
        }

       order.setOrderStatus("CONFIRMED");
Order confirmedOrder = orderRepository.save(order);

// Publish the confirmation event without losing the saved order
// if Kafka is temporarily unavailable.
try {
    orderEventPublisher.publishOrderConfirmed(confirmedOrder.getId());
} catch (RuntimeException exception) {
    System.err.println("Kafka event publishing failed for order "
            + confirmedOrder.getId() + ": " + exception.getMessage());
}

// Create a notification record after order confirmation.
Map<String, Object> notificationRequest = Map.of(
        "orderId", confirmedOrder.getId(),
        "recipient", "shopsphere.test@example.com",
        "type", "ORDER_CONFIRMED",
        "message", "Your order #" + confirmedOrder.getId()
                + " has been confirmed."
);

try {
    notificationRestClient.post()
            .uri("/notifications")
            .contentType(MediaType.APPLICATION_JSON)
            .body(notificationRequest)
            .retrieve()
            .toBodilessEntity();
} catch (RuntimeException exception) {
    // Keep the confirmed order even if notification creation fails.
    System.err.println(
            "Notification creation failed for order "
                    + confirmedOrder.getId()
                    + ": " + exception.getMessage()
    );
}

return confirmedOrder;
    }

    public Order updateOrder(Long id, Order updatedOrder) {

        Order existingOrder =
                orderRepository.findById(id).orElse(null);

        if (existingOrder == null || updatedOrder == null) {
            return null;
        }

        existingOrder.setProductId(updatedOrder.getProductId());
        existingOrder.setProductName(updatedOrder.getProductName());
        existingOrder.setQuantity(updatedOrder.getQuantity());
        existingOrder.setPrice(updatedOrder.getPrice());

        // Preserve the existing order status.
        return orderRepository.save(existingOrder);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}