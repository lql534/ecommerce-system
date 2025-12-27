package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderDTO;
import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product testProduct;
    private CartItem testCartItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(100);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setUserId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20251227123456ABC123");
        testOrder.setUserId(1L);
        testOrder.setTotalAmount(new BigDecimal("199.98"));
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("从购物车创建订单 - 成功")
    void createOrderFromCart_Success_ShouldCreateOrder() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCartItem));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.createOrderFromCart(1L, "北京市朝阳区", "请尽快发货");

        assertNotNull(result);
        verify(cartItemRepository, times(1)).deleteByUserId(1L);
    }

    @Test
    @DisplayName("从购物车创建订单 - 购物车为空")
    void createOrderFromCart_EmptyCart_ShouldThrowException() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(new ArrayList<>());

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(1L, "地址", "备注");
        });
    }

    @Test
    @DisplayName("从购物车创建订单 - 库存不足")
    void createOrderFromCart_InsufficientStock_ShouldThrowException() {
        testProduct.setStock(1);
        testCartItem.setQuantity(5);
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCartItem));

        assertThrows(RuntimeException.class, () -> {
            orderService.createOrderFromCart(1L, "地址", "备注");
        });
    }

    @Test
    @DisplayName("根据ID获取订单")
    void getOrderById_ShouldReturnOrder() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(testOrder));

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals("ORD20251227123456ABC123", result.get().getOrderNo());
    }

    @Test
    @DisplayName("根据订单号获取订单")
    void getOrderByOrderNo_ShouldReturnOrder() {
        when(orderRepository.findByOrderNo("ORD20251227123456ABC123")).thenReturn(Optional.of(testOrder));

        Optional<Order> result = orderService.getOrderByOrderNo("ORD20251227123456ABC123");

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("获取用户订单列表")
    void getUserOrders_ShouldReturnOrders() {
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Arrays.asList(testOrder));

        List<Order> result = orderService.getUserOrders(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("分页获取用户订单")
    void getUserOrders_Pageable_ShouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderRepository.findByUserId(1L, pageable)).thenReturn(orderPage);

        Page<Order> result = orderService.getUserOrders(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("分页获取所有订单")
    void getAllOrders_ShouldReturnPagedOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        Page<Order> result = orderService.getAllOrders(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("根据状态获取订单")
    void getOrdersByStatus_ShouldReturnFilteredOrders() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder), pageable, 1);
        when(orderRepository.findByStatus(Order.OrderStatus.PENDING, pageable)).thenReturn(orderPage);

        Page<Order> result = orderService.getOrdersByStatus(Order.OrderStatus.PENDING, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("更新订单状态 - 待支付到已支付")
    void updateOrderStatus_PendingToPaid_ShouldSucceed() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.updateOrderStatus(1L, Order.OrderStatus.PAID);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.PAID, testOrder.getStatus());
        assertNotNull(testOrder.getPaidAt());
    }

    @Test
    @DisplayName("更新订单状态 - 无效状态转换")
    void updateOrderStatus_InvalidTransition_ShouldThrowException() {
        testOrder.setStatus(Order.OrderStatus.DELIVERED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        assertThrows(RuntimeException.class, () -> {
            orderService.updateOrderStatus(1L, Order.OrderStatus.PENDING);
        });
    }

    @Test
    @DisplayName("取消订单 - 成功")
    void cancelOrder_ShouldCancelAndRestoreStock() {
        OrderItem orderItem = new OrderItem();
        orderItem.setProductId(1L);
        orderItem.setQuantity(2);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Arrays.asList(orderItem));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Order result = orderService.cancelOrder(1L);

        assertNotNull(result);
        assertEquals(Order.OrderStatus.CANCELLED, testOrder.getStatus());
    }

    @Test
    @DisplayName("取消订单 - 订单不存在")
    void cancelOrder_OrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(999L);
        });
    }
}
