package com.shopsphere.orderservice1.service;

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

    public OrderService(
            OrderRepository orderRepository,
            @Qualifier("productRestClient") RestClient productRestClient,
            @Qualifier("paymentRestClient") RestClient paymentRestClient) {

        this.orderRepository = orderRepository;
        this.productRestClient = productRestClient;
        this.paymentRestClient = paymentRestClient;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(CreateOrderRequest request) {

        // Step 1: Get product details from Product Service
        ProductResponse product = productRestClient.get()
                .uri("/products/" + request.getProductId())
                .retrieve()
                .body(ProductResponse.class);

        if (product == null) {
            return null;
        }

        // Step 2: Validate quantity
        if (request.getQuantity() <= 0) {
            return null;
        }

        // Step 3: Check available stock
        if (product.getQuantity() < request.getQuantity()) {
            return null;
        }

        // Step 4: Reduce product stock
        productRestClient.put()
                .uri("/products/"
                        + request.getProductId()
                        + "/reduce-stock?quantity="
                        + request.getQuantity())
                .retrieve()
                .body(ProductResponse.class);

        // Step 5: Create and save the order
        Order order = new Order(
                request.getProductId(),
                product.getName(),
                request.getQuantity(),
                product.getPrice()
        );

        Order savedOrder = orderRepository.save(order);

        // Step 6: Create a payment record for this order
        Map<String, Object> paymentRequest = Map.of(
                "orderId", savedOrder.getId(),
                "amount", savedOrder.getPrice() * savedOrder.getQuantity(),
                "paymentMethod", "UPI"
        );

        paymentRestClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequest)
                .retrieve()
                .body(PaymentResponse.class);

        return savedOrder;
    }

    public Order updateOrder(Long id, Order updatedOrder) {
        Order existingOrder =
                orderRepository.findById(id).orElse(null);

        if (existingOrder == null) {
            return null;
        }

        existingOrder.setProductId(updatedOrder.getProductId());
        existingOrder.setProductName(updatedOrder.getProductName());
        existingOrder.setQuantity(updatedOrder.getQuantity());
        existingOrder.setPrice(updatedOrder.getPrice());

        return orderRepository.save(existingOrder);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
