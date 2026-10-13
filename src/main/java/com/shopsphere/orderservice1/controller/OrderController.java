
package com.shopsphere.orderservice1.controller;

import com.shopsphere.orderservice1.dto.CreateOrderRequest;
import com.shopsphere.orderservice1.entity.Order;
import com.shopsphere.orderservice1.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PostMapping
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @PutMapping("/{id}")
    public Order updateOrder(
            @PathVariable Long id,
            @RequestBody Order updatedOrder) {

        return orderService.updateOrder(id, updatedOrder);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
    }

    // Confirm an order after verifying successful payment
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {

        Order order = orderService.confirmOrder(id);

        if (order == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "message",
                            "Order not found or payment is not successful"
                    ));
        }

        return ResponseEntity.ok(order);
    }
}
