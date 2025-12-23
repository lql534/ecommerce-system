package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.status = :status ORDER BY o.createdAt DESC")
    List<Order> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
