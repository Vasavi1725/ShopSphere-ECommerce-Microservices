package com.shopsphere.orderservice1.service;

import com.shopsphere.orderservice1.dto.CreateOrderRequest;
import com.shopsphere.orderservice1.dto.ProductResponse;
import com.shopsphere.orderservice1.entity.Order;
import com.shopsphere.orderservice1.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestClient restClient;

    public OrderService(
            OrderRepository orderRepository,
            RestClient restClient) {

        this.orderRepository = orderRepository;
        this.restClient = restClient;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    public Order createOrder(CreateOrderRequest request) {

        // Step 1: Get product details from Product Service
        ProductResponse product = restClient.get()
                .uri("/products/" + request.getProductId())
                .retrieve()
                .body(ProductResponse.class);

        // Product not found
        if (product == null) {
            return null;
        }

        // Step 2: Check quantity
        if (request.getQuantity() <= 0) {
            return null;
        }

        // Step 3: Check available stock
        if (product.getQuantity() < request.getQuantity()) {
            return null;
        }

        // Step 4: Reduce product stock
        restClient.put()
                .uri("/products/"
                        + request.getProductId()
                        + "/reduce-stock?quantity="
                        + request.getQuantity())
                .retrieve()
                .body(ProductResponse.class);

        // Step 5: Create order using product details
        Order order = new Order(
                request.getProductId(),
                product.getName(),
                request.getQuantity(),
                product.getPrice()
        );

        // Step 6: Save order
        return orderRepository.save(order);
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