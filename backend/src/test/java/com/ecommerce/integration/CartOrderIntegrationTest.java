package com.ecommerce.integration;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.dto.CreateOrderDTO;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.User;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("购物车与订单集成测试")
class CartOrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private static Long testUserId;
    private static Long testProductId;
    private static String createdOrderNo;

    @BeforeAll
    static void setUpAll(@Autowired UserRepository userRepository,
                         @Autowired ProductRepository productRepository,
                         @Autowired CartItemRepository cartItemRepository) {
        cartItemRepository.deleteAll();

        User user = userRepository.findByUsername("cartuser").orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername("cartuser");
            newUser.setPassword("cart123");
            newUser.setEmail("cart@example.com");
            newUser.setRole("USER");
            newUser.setStatus(1);
            return userRepository.save(newUser);
        });
        testUserId = user.getId();

        Product product = new Product();
        product.setName("购物车测试商品");
        product.setDescription("用于购物车测试");
        product.setPrice(new BigDecimal("88.88"));
        product.setStock(100);
        product.setCategory("测试");
        Product savedProduct = productRepository.save(product);
        testProductId = savedProduct.getId();
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("集成测试 - 添加商品到购物车")
    void addToCart_Integration_ShouldAddItem() throws Exception {
        CartItemDTO dto = new CartItemDTO();
        dto.setProductId(testProductId);
        dto.setQuantity(2);

        mockMvc.perform(post("/api/cart/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2));

        Assertions.assertFalse(cartItemRepository.findByUserId(testUserId).isEmpty());
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("集成测试 - 查看购物车")
    void getCart_Integration_ShouldReturnItems() throws Exception {
        mockMvc.perform(get("/api/cart/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("集成测试 - 更新购物车数量")
    void updateCart_Integration_ShouldUpdateQuantity() throws Exception {
        Map<String, Integer> body = new HashMap<>();
        body.put("quantity", 5);

        mockMvc.perform(put("/api/cart/" + testUserId + "/" + testProductId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("集成测试 - 从购物车创建订单")
    void createOrderFromCart_Integration_ShouldCreateOrder() throws Exception {
        CreateOrderDTO dto = new CreateOrderDTO();
        dto.setUserId(testUserId);
        dto.setShippingAddress("北京市海淀区测试地址");
        dto.setRemark("集成测试订单");

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNo").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        com.ecommerce.entity.Order order = objectMapper.readValue(response, com.ecommerce.entity.Order.class);
        createdOrderNo = order.getOrderNo();

        Assertions.assertTrue(cartItemRepository.findByUserId(testUserId).isEmpty());

        Product product = productRepository.findById(testProductId).orElseThrow();
        Assertions.assertEquals(95, product.getStock());
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("集成测试 - 查询订单详情")
    void getOrder_Integration_ShouldReturnOrder() throws Exception {
        com.ecommerce.entity.Order order = orderRepository.findByOrderNo(createdOrderNo).orElseThrow();

        mockMvc.perform(get("/api/orders/" + order.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNo").value(createdOrderNo))
                .andExpect(jsonPath("$.shippingAddress").value("北京市海淀区测试地址"));
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("集成测试 - 查询用户订单列表")
    void getUserOrders_Integration_ShouldReturnOrders() throws Exception {
        mockMvc.perform(get("/api/orders/user/" + testUserId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("集成测试 - 更新订单状态为已支付")
    void updateOrderStatus_Integration_ShouldUpdateToPaid() throws Exception {
        com.ecommerce.entity.Order order = orderRepository.findByOrderNo(createdOrderNo).orElseThrow();

        mockMvc.perform(put("/api/orders/" + order.getId() + "/status")
                        .param("status", "PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        com.ecommerce.entity.Order updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        Assertions.assertEquals(com.ecommerce.entity.Order.OrderStatus.PAID, updatedOrder.getStatus());
        Assertions.assertNotNull(updatedOrder.getPaidAt());
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("集成测试 - 取消订单并恢复库存")
    void cancelOrder_Integration_ShouldRestoreStock() throws Exception {
        CartItemDTO dto = new CartItemDTO();
        dto.setProductId(testProductId);
        dto.setQuantity(3);

        mockMvc.perform(post("/api/cart/" + testUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        CreateOrderDTO orderDto = new CreateOrderDTO();
        orderDto.setUserId(testUserId);
        orderDto.setShippingAddress("取消测试地址");

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        com.ecommerce.entity.Order newOrder = objectMapper.readValue(response, com.ecommerce.entity.Order.class);

        Product beforeCancel = productRepository.findById(testProductId).orElseThrow();
        int stockBeforeCancel = beforeCancel.getStock();

        mockMvc.perform(post("/api/orders/" + newOrder.getId() + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Product afterCancel = productRepository.findById(testProductId).orElseThrow();
        Assertions.assertEquals(stockBeforeCancel + 3, afterCancel.getStock());
    }
}
