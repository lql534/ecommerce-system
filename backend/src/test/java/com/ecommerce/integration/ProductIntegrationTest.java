package com.ecommerce.integration;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("商品模块集成测试")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private static Long createdProductId;

    @BeforeEach
    void setUp() {
        // 清理测试数据
    }

    @Test
    @Order(1)
    @DisplayName("集成测试 - 创建商品")
    void createProduct_Integration_ShouldPersistToDatabase() throws Exception {
        Product product = new Product();
        product.setName("集成测试商品");
        product.setDescription("这是集成测试创建的商品");
        product.setPrice(new BigDecimal("199.99"));
        product.setStock(50);
        product.setCategory("测试分类");

        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("集成测试商品"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        Product createdProduct = objectMapper.readValue(response, Product.class);
        createdProductId = createdProduct.getId();

        // 验证数据库中确实存在
        Assertions.assertTrue(productRepository.existsById(createdProductId));
    }

    @Test
    @Order(2)
    @DisplayName("集成测试 - 查询商品列表")
    void getProducts_Integration_ShouldReturnFromDatabase() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @Order(3)
    @DisplayName("集成测试 - 根据ID查询商品")
    void getProductById_Integration_ShouldReturnProduct() throws Exception {
        if (createdProductId != null) {
            mockMvc.perform(get("/api/products/" + createdProductId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("集成测试商品"));
        }
    }

    @Test
    @Order(4)
    @DisplayName("集成测试 - 更新商品")
    void updateProduct_Integration_ShouldUpdateInDatabase() throws Exception {
        if (createdProductId != null) {
            Product updateProduct = new Product();
            updateProduct.setName("更新后的商品名称");
            updateProduct.setPrice(new BigDecimal("299.99"));
            updateProduct.setStock(100);
            updateProduct.setCategory("更新分类");

            mockMvc.perform(put("/api/products/" + createdProductId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateProduct)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("更新后的商品名称"));

            // 验证数据库更新
            Product dbProduct = productRepository.findById(createdProductId).orElse(null);
            Assertions.assertNotNull(dbProduct);
            Assertions.assertEquals("更新后的商品名称", dbProduct.getName());
        }
    }

    @Test
    @Order(5)
    @DisplayName("集成测试 - 搜索商品")
    void searchProducts_Integration_ShouldReturnMatchingProducts() throws Exception {
        mockMvc.perform(get("/api/products")
                        .param("keyword", "更新"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @Order(6)
    @DisplayName("集成测试 - 删除商品")
    void deleteProduct_Integration_ShouldRemoveFromDatabase() throws Exception {
        if (createdProductId != null) {
            mockMvc.perform(delete("/api/products/" + createdProductId))
                    .andExpect(status().isNoContent());

            // 验证数据库中已删除
            Assertions.assertFalse(productRepository.existsById(createdProductId));
        }
    }
}
