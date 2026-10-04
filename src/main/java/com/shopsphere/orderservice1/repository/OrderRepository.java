package com.shopsphere.orderservice1.repository;

import com.shopsphere.orderservice1.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}