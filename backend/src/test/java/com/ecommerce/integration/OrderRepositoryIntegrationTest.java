package com.ecommerce.integration;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("订单Repository集成测试")
class OrderRepositoryIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        
        testOrder = new Order();
        testOrder.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        testOrder.setUserId(1L);
        testOrder.setTotalAmount(new BigDecimal("299.99"));
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setShippingAddress("北京市朝阳区测试地址");
        testOrder = orderRepository.save(testOrder);
    }

    @Test
    @DisplayName("保存订单 - 应成功保存并生成ID")
    void save_ShouldPersistOrder() {
        Order newOrder = new Order();
        newOrder.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        newOrder.setUserId(2L);
        newOrder.setTotalAmount(new BigDecimal("199.99"));
        newOrder.setStatus(Order.OrderStatus.PENDING);

        Order saved = orderRepository.save(newOrder);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    @Test
    @DisplayName("根据订单号查询 - 存在时应返回订单")
    void findByOrderNo_WhenExists_ShouldReturnOrder() {
        Optional<Order> found = orderRepository.findByOrderNo(testOrder.getOrderNo());

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("根据订单号查询 - 不存在时应返回空")
    void findByOrderNo_WhenNotExists_ShouldReturnEmpty() {
        Optional<Order> found = orderRepository.findByOrderNo("NONEXISTENT");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("根据用户ID查询订单列表")
    void findByUserId_ShouldReturnUserOrders() {
        // 添加更多订单
        Order order2 = new Order();
        order2.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order2.setUserId(1L);
        order2.setTotalAmount(new BigDecimal("99.99"));
        order2.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order2);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(1L);

        assertThat(orders).hasSize(2);
    }

    @Test
    @DisplayName("分页查询用户订单")
    void findByUserId_WithPagination_ShouldReturnPagedResults() {
        // 添加多个订单
        for (int i = 0; i < 15; i++) {
            Order order = new Order();
            order.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            order.setUserId(1L);
            order.setTotalAmount(new BigDecimal("10.00"));
            order.setStatus(Order.OrderStatus.PENDING);
            orderRepository.save(order);
        }

        Page<Order> page = orderRepository.findByUserId(1L, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(10);
        assertThat(page.getTotalElements()).isEqualTo(16); // 15 + 1(testOrder)
    }

    @Test
    @DisplayName("根据状态查询订单")
    void findByStatus_ShouldReturnOrdersWithStatus() {
        Order paidOrder = new Order();
        paidOrder.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        paidOrder.setUserId(2L);
        paidOrder.setTotalAmount(new BigDecimal("50.00"));
        paidOrder.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(paidOrder);

        Page<Order> pendingOrders = orderRepository.findByStatus(
            Order.OrderStatus.PENDING, PageRequest.of(0, 10));
        Page<Order> paidOrders = orderRepository.findByStatus(
            Order.OrderStatus.PAID, PageRequest.of(0, 10));

        assertThat(pendingOrders.getContent()).hasSize(1);
        assertThat(paidOrders.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("更新订单状态 - 应成功更新")
    void updateStatus_ShouldModifyOrderStatus() {
        testOrder.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(testOrder);

        Order updated = orderRepository.findById(testOrder.getId()).orElseThrow();

        assertThat(updated.getStatus()).isEqualTo(Order.OrderStatus.PAID);
    }

    @Test
    @DisplayName("订单状态流转 - PENDING -> PAID -> SHIPPED -> DELIVERED")
    void orderStatusFlow_ShouldTransitionCorrectly() {
        // PENDING -> PAID
        testOrder.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(testOrder);
        assertThat(orderRepository.findById(testOrder.getId()).get().getStatus())
            .isEqualTo(Order.OrderStatus.PAID);

        // PAID -> SHIPPED
        testOrder.setStatus(Order.OrderStatus.SHIPPED);
        orderRepository.save(testOrder);
        assertThat(orderRepository.findById(testOrder.getId()).get().getStatus())
            .isEqualTo(Order.OrderStatus.SHIPPED);

        // SHIPPED -> DELIVERED
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        orderRepository.save(testOrder);
        assertThat(orderRepository.findById(testOrder.getId()).get().getStatus())
            .isEqualTo(Order.OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("取消订单 - 应成功更新为CANCELLED状态")
    void cancelOrder_ShouldSetStatusToCancelled() {
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(testOrder);

        Order cancelled = orderRepository.findById(testOrder.getId()).orElseThrow();

        assertThat(cancelled.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("删除订单 - 应成功删除")
    void delete_ShouldRemoveOrder() {
        Long id = testOrder.getId();
        orderRepository.deleteById(id);

        Optional<Order> deleted = orderRepository.findById(id);

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("按创建时间排序查询")
    void findAll_SortedByCreatedAt_ShouldReturnSortedResults() {
        Order order2 = new Order();
        order2.setOrderNo("ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order2.setUserId(1L);
        order2.setTotalAmount(new BigDecimal("50.00"));
        order2.setStatus(Order.OrderStatus.PENDING);
        orderRepository.save(order2);

        Page<Order> orders = orderRepository.findAll(
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

        assertThat(orders.getContent()).hasSize(2);
    }
}
