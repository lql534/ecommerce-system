package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单CRUD接口")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "创建订单", description = "从购物车或直接创建订单")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderDTO dto) {
        try {
            Order order = orderService.createOrder(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详情")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/no/{orderNo}")
    @Operation(summary = "根据订单号查询", description = "根据订单号获取订单")
    public ResponseEntity<?> getOrderByNo(@PathVariable String orderNo) {
        return orderService.getOrderByOrderNo(orderNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户订单", description = "获取用户的所有订单")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/user/{userId}/page")
    @Operation(summary = "分页获取用户订单", description = "分页获取用户订单历史")
    public ResponseEntity<Page<Order>> getUserOrdersPage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(orderService.getUserOrders(userId, pageable));
    }

    @GetMapping
    @Operation(summary = "获取所有订单", description = "管理员获取所有订单")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(orderService.getOrdersByStatus(orderStatus, pageable));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.ok(orderService.getAllOrders(pageable));
            }
        }
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态", description = "更新订单状态（付款、发货、送达等）")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            String statusStr = body.get("status");
            Order.OrderStatus status = Order.OrderStatus.valueOf(statusStr.toUpperCase());
            Order order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "无效的订单状态"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单", description = "模拟支付订单")
    public ResponseEntity<?> payOrder(@PathVariable Long id) {
        try {
            Order order = orderService.updateOrderStatus(id, Order.OrderStatus.PAID);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/ship")
    @Operation(summary = "发货", description = "订单发货")
    public ResponseEntity<?> shipOrder(@PathVariable Long id) {
        try {
            Order order = orderService.updateOrderStatus(id, Order.OrderStatus.SHIPPED);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/deliver")
    @Operation(summary = "确认送达", description = "确认订单送达")
    public ResponseEntity<?> deliverOrder(@PathVariable Long id) {
        try {
            Order order = orderService.updateOrderStatus(id, Order.OrderStatus.DELIVERED);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消订单并恢复库存")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        try {
            Order order = orderService.cancelOrder(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
