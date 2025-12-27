package com.ecommerce.controller;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@DisplayName("购物车控制器集成测试")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartItem testCartItem;
    private Product testProduct;

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
    }

    @Test
    @DisplayName("GET /api/cart/{userId} - 获取购物车")
    void getCart_ShouldReturnCartItems() throws Exception {
        when(cartService.getCartItems(1L)).thenReturn(Arrays.asList(testCartItem));

        mockMvc.perform(get("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    @DisplayName("POST /api/cart/{userId} - 添加商品到购物车")
    void addToCart_ShouldReturnCartItem() throws Exception {
        when(cartService.addToCart(eq(1L), eq(1L), eq(2))).thenReturn(testCartItem);

        CartItemDTO dto = new CartItemDTO();
        dto.setProductId(1L);
        dto.setQuantity(2);

        mockMvc.perform(post("/api/cart/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    @DisplayName("POST /api/cart/{userId} - 添加商品库存不足")
    void addToCart_InsufficientStock_ShouldReturnBadRequest() throws Exception {
        when(cartService.addToCart(eq(1L), eq(1L), eq(200)))
                .thenThrow(new RuntimeException("库存不足"));

        CartItemDTO dto = new CartItemDTO();
        dto.setProductId(1L);
        dto.setQuantity(200);

        mockMvc.perform(post("/api/cart/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("库存不足"));
    }

    @Test
    @DisplayName("PUT /api/cart/{userId}/{productId} - 更新购物车数量")
    void updateCartItem_ShouldReturnUpdatedItem() throws Exception {
        testCartItem.setQuantity(5);
        when(cartService.updateCartItem(eq(1L), eq(1L), eq(5))).thenReturn(testCartItem);

        Map<String, Integer> body = new HashMap<>();
        body.put("quantity", 5);

        mockMvc.perform(put("/api/cart/1/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    @DisplayName("PUT /api/cart/{userId}/{productId} - 数量为0删除商品")
    void updateCartItem_ZeroQuantity_ShouldRemoveItem() throws Exception {
        when(cartService.updateCartItem(eq(1L), eq(1L), eq(0))).thenReturn(null);

        Map<String, Integer> body = new HashMap<>();
        body.put("quantity", 0);

        mockMvc.perform(put("/api/cart/1/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("商品已从购物车移除"));
    }

    @Test
    @DisplayName("DELETE /api/cart/{userId}/{productId} - 移除商品")
    void removeFromCart_ShouldReturnSuccess() throws Exception {
        doNothing().when(cartService).removeFromCart(1L, 1L);

        mockMvc.perform(delete("/api/cart/1/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("商品已移除"));
    }

    @Test
    @DisplayName("DELETE /api/cart/{userId} - 清空购物车")
    void clearCart_ShouldReturnSuccess() throws Exception {
        doNothing().when(cartService).clearCart(1L);

        mockMvc.perform(delete("/api/cart/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("购物车已清空"));
    }
}
