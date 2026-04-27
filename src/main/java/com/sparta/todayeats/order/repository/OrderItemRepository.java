package com.sparta.todayeats.order.repository;

import com.sparta.todayeats.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * 주문 항목 레포지토리
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}