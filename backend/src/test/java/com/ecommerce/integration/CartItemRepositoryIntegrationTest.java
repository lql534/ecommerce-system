package com.ecommerce.integration;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("购物车Repository集成测试")
class CartItemRepositoryIntegrationTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll();
        productRepository.deleteAll();
        
        // 创建测试商品
        testProduct = new Product();
        testProduct.setName("测试商品");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(100);
        testProduct.setCategory("电子产品");
        testProduct = productRepository.save(testProduct);

        // 创建测试购物车项
        testCartItem = new CartItem();
        testCartItem.setUserId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem = cartItemRepository.save(testCartItem);
    }

    @Test
    @DisplayName("添加商品到购物车 - 应成功保存")
    void save_ShouldPersistCartItem() {
        Product product2 = new Product();
        product2.setName("商品2");
        product2.setPrice(new BigDecimal("199.99"));
        product2.setStock(50);
        product2 = productRepository.save(product2);

        CartItem newItem = new CartItem();
        newItem.setUserId(1L);
        newItem.setProduct(product2);
        newItem.setQuantity(1);

        CartItem saved = cartItemRepository.save(newItem);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("根据用户ID查询购物车 - 应返回用户的所有购物车项")
    void findByUserId_ShouldReturnUserCartItems() {
        List<CartItem> items = cartItemRepository.findByUserId(1L);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getProduct().getName()).isEqualTo("测试商品");
    }

    @Test
    @DisplayName("根据用户ID和商品ID查询 - 存在时应返回购物车项")
    void findByUserIdAndProductId_WhenExists_ShouldReturnCartItem() {
        Optional<CartItem> found = cartItemRepository.findByUserIdAndProductId(1L, testProduct.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getQuantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("根据用户ID和商品ID查询 - 不存在时应返回空")
    void findByUserIdAndProductId_WhenNotExists_ShouldReturnEmpty() {
        Optional<CartItem> found = cartItemRepository.findByUserIdAndProductId(1L, 99999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("更新购物车数量 - 应成功更新")
    void updateQuantity_ShouldModifyCartItem() {
        testCartItem.setQuantity(5);
        cartItemRepository.save(testCartItem);

        CartItem updated = cartItemRepository.findById(testCartItem.getId()).orElseThrow();

        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("删除购物车项 - 应成功删除")
    void delete_ShouldRemoveCartItem() {
        Long id = testCartItem.getId();
        cartItemRepository.deleteById(id);

        Optional<CartItem> deleted = cartItemRepository.findById(id);

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("清空用户购物车 - 应删除用户所有购物车项")
    void deleteByUserId_ShouldRemoveAllUserCartItems() {
        // 添加更多购物车项
        Product product2 = new Product();
        product2.setName("商品2");
        product2.setPrice(new BigDecimal("50.00"));
        product2.setStock(30);
        product2 = productRepository.save(product2);

        CartItem item2 = new CartItem();
        item2.setUserId(1L);
        item2.setProduct(product2);
        item2.setQuantity(3);
        cartItemRepository.save(item2);

        // 清空购物车
        cartItemRepository.deleteByUserId(1L);

        List<CartItem> remaining = cartItemRepository.findByUserId(1L);

        assertThat(remaining).isEmpty();
    }

    @Test
    @DisplayName("多用户购物车隔离 - 不同用户购物车应独立")
    void multiUserCart_ShouldBeIsolated() {
        // 用户2添加购物车
        CartItem user2Item = new CartItem();
        user2Item.setUserId(2L);
        user2Item.setProduct(testProduct);
        user2Item.setQuantity(1);
        cartItemRepository.save(user2Item);

        List<CartItem> user1Items = cartItemRepository.findByUserId(1L);
        List<CartItem> user2Items = cartItemRepository.findByUserId(2L);

        assertThat(user1Items).hasSize(1);
        assertThat(user2Items).hasSize(1);
        assertThat(user1Items.get(0).getQuantity()).isEqualTo(2);
        assertThat(user2Items.get(0).getQuantity()).isEqualTo(1);
    }

    @Test
    @DisplayName("购物车商品关联 - 应正确加载商品信息")
    void cartItemProductRelation_ShouldLoadProductInfo() {
        CartItem found = cartItemRepository.findById(testCartItem.getId()).orElseThrow();

        assertThat(found.getProduct()).isNotNull();
        assertThat(found.getProduct().getName()).isEqualTo("测试商品");
        assertThat(found.getProduct().getPrice()).isEqualByComparingTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("统计用户购物车商品数量")
    void countByUserId_ShouldReturnCorrectCount() {
        long count = cartItemRepository.findByUserId(1L).size();

        assertThat(count).isEqualTo(1);
    }
}
