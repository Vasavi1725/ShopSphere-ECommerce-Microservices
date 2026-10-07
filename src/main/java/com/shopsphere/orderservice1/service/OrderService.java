package com.shopsphere.orderservice1.service;

import com.shopsphere.orderservice1.entity.Order;
import com.shopsphere.orderservice1.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Get order by ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id).orElse(null);
    }

    // Create order
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    // Update order
    public Order updateOrder(Long id, Order updatedOrder) {

        Order existingOrder = orderRepository.findById(id).orElse(null);

        if (existingOrder == null) {
            return null;
        }

        existingOrder.setProductName(updatedOrder.getProductName());
        existingOrder.setQuantity(updatedOrder.getQuantity());
        existingOrder.setPrice(updatedOrder.getPrice());

        return orderRepository.save(existingOrder);
    }

    // Delete order
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}