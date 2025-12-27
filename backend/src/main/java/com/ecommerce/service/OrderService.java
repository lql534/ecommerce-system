package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderDTO;
import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrderFromCart(Long userId, String shippingAddress, String remark) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }

        // 创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setShippingAddress(shippingAddress);
        order.setRemark(remark);
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 处理每个购物车项
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            
            // 库存校验
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足，当前库存: " + product.getStock());
            }

            // 扣减库存
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            // 创建订单项
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            
            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        // 清空购物车
        cartItemRepository.deleteByUserId(userId);

        log.info("订单创建成功: {}", savedOrder.getOrderNo());
        return savedOrder;
    }

    /**
     * 直接创建订单（不通过购物车）
     */
    @Transactional
    public Order createOrder(CreateOrderDTO dto) {
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            return createOrderFromCart(dto.getUserId(), dto.getShippingAddress(), dto.getRemark());
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(dto.getUserId());
        order.setShippingAddress(dto.getShippingAddress());
        order.setRemark(dto.getRemark());
        order.setStatus(Order.OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateOrderDTO.OrderItemDTO itemDTO : dto.getItems()) {
            Product product = productRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + itemDTO.getProductId()));

            if (product.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("商品 [" + product.getName() + "] 库存不足");
            }

            product.setStock(product.getStock() - itemDTO.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setProductPrice(product.getPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity())));

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    public Optional<Order> getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    /**
     * 更新订单状态
     */
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Order.OrderStatus currentStatus = order.getStatus();

        // 状态流转校验
        if (!isValidStatusTransition(currentStatus, newStatus)) {
            throw new RuntimeException("无效的状态变更: " + currentStatus + " -> " + newStatus);
        }

        order.setStatus(newStatus);

        // 记录时间
        switch (newStatus) {
            case PAID -> order.setPaidAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
            case CANCELLED -> {
                // 取消订单时恢复库存
                restoreStock(order);
            }
        }

        return orderRepository.save(order);
    }

    /**
     * 取消订单
     */
    @Transactional
    public Order cancelOrder(Long orderId) {
        return updateOrderStatus(orderId, Order.OrderStatus.CANCELLED);
    }

    /**
     * 恢复库存
     */
    private void restoreStock(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        for (OrderItem item : items) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            });
        }
    }

    /**
     * 校验状态流转是否合法
     */
    private boolean isValidStatusTransition(Order.OrderStatus from, Order.OrderStatus to) {
        return switch (from) {
            case PENDING -> to == Order.OrderStatus.PAID || to == Order.OrderStatus.CANCELLED;
            case PAID -> to == Order.OrderStatus.SHIPPED || to == Order.OrderStatus.CANCELLED;
            case SHIPPED -> to == Order.OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "ORD" + timestamp + uuid;
    }
}
