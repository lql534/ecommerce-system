package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@DisplayName("商品控制器集成测试")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        testProduct.setDescription("测试描述");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(100);
        testProduct.setCategory("电子产品");
    }

    @Test
    @DisplayName("GET /api/products - 获取商品列表")
    void getProducts_ShouldReturnProductList() throws Exception {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct), PageRequest.of(0, 12), 1);
        when(productService.searchProducts(any(), any(), any())).thenReturn(productPage);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].name").value("测试商品"));
    }

    @Test
    @DisplayName("GET /api/products/{id} - 获取商品详情")
    void getProduct_WhenExists_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试商品"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    @DisplayName("GET /api/products/{id} - 商品不存在返回404")
    void getProduct_WhenNotExists_ShouldReturn404() throws Exception {
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/products - 创建商品")
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("测试商品"));
    }

    @Test
    @DisplayName("PUT /api/products/{id} - 更新商品")
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() throws Exception {
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(Optional.of(testProduct));

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("测试商品"));
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - 删除商品")
    void deleteProduct_WhenExists_ShouldReturn204() throws Exception {
        when(productService.deleteProduct(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - 删除不存在的商品返回404")
    void deleteProduct_WhenNotExists_ShouldReturn404() throws Exception {
        when(productService.deleteProduct(999L)).thenReturn(false);

        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }
}
