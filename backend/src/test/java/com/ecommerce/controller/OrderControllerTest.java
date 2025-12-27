package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderDTO;
import com.ecommerce.entity.Order;
import com.ecommerce.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("订单控制器测试")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("ORD20251227123456ABC123");
        testOrder.setUserId(1L);
        testOrder.setTotalAmount(new BigDecimal("199.98"));
        testOrder.setStatus(Order.OrderStatus.PENDING);
        testOrder.setShippingAddress("北京市朝阳区");
        testOrder.setItems(new ArrayList<>());
    }

    @Test
    @DisplayName("POST /api/orders - 创建订单")
    void createOrder_ShouldReturnCreatedOrder() throws Exception {
        when(orderService.createOrder(any(CreateOrderDTO.class))).thenReturn(testOrder);

        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setUserId(1L);
        dto.setShippingAddress("北京市朝阳区");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNo").value("ORD20251227123456ABC123"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - 获取订单详情")
    void getOrder_WhenExists_ShouldReturnOrder() throws Exception {
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(testOrder));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value("ORD20251227123456ABC123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} - 订单不存在")
    void getOrder_WhenNotExists_ShouldReturn404() throws Exception {
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId} - 获取用户订单列表")
    void getUserOrders_ShouldReturnOrders() throws Exception {
        // 实际API返回List<Order>而不是Page<Order>
        List<Order> orderList = Arrays.asList(testOrder);
        when(orderService.getUserOrders(1L)).thenReturn(orderList);

        mockMvc.perform(get("/api/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].orderNo").value("ORD20251227123456ABC123"));
    }

    @Test
    @DisplayName("GET /api/orders/user/{userId}/page - 分页获取用户订单")
    void getUserOrdersPage_ShouldReturnPagedOrders() throws Exception {
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder));
        when(orderService.getUserOrders(eq(1L), any(Pageable.class))).thenReturn(orderPage);

        mockMvc.perform(get("/api/orders/user/1/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].orderNo").value("ORD20251227123456ABC123"));
    }

    @Test
    @DisplayName("PUT /api/orders/{id}/status - 更新订单状态")
    void updateOrderStatus_ShouldReturnUpdatedOrder() throws Exception {
        testOrder.setStatus(Order.OrderStatus.PAID);
        when(orderService.updateOrderStatus(1L, Order.OrderStatus.PAID)).thenReturn(testOrder);

        // 实际API使用@RequestBody Map<String, String>接收status
        Map<String, String> body = Map.of("status", "PAID");

        mockMvc.perform(put("/api/orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel - 取消订单")
    void cancelOrder_ShouldReturnCancelledOrder() throws Exception {
        testOrder.setStatus(Order.OrderStatus.CANCELLED);
        when(orderService.cancelOrder(1L)).thenReturn(testOrder);

        mockMvc.perform(post("/api/orders/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
