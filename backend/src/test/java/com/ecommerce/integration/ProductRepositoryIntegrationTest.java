package com.ecommerce.integration;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("商品Repository集成测试")
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        
        testProduct = new Product();
        testProduct.setName("测试商品");
        testProduct.setDescription("这是一个测试商品");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(100);
        testProduct.setCategory("电子产品");
        testProduct.setImageUrl("/images/test.jpg");
        testProduct = productRepository.save(testProduct);
    }

    @Test
    @DisplayName("保存商品 - 应成功保存并生成ID")
    void save_ShouldPersistProduct() {
        Product newProduct = new Product();
        newProduct.setName("新商品");
        newProduct.setPrice(new BigDecimal("199.99"));
        newProduct.setStock(50);
        newProduct.setCategory("服装");

        Product saved = productRepository.save(newProduct);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("新商品");
    }

    @Test
    @DisplayName("根据ID查询 - 存在时应返回商品")
    void findById_WhenExists_ShouldReturnProduct() {
        Optional<Product> found = productRepository.findById(testProduct.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("测试商品");
    }

    @Test
    @DisplayName("根据ID查询 - 不存在时应返回空")
    void findById_WhenNotExists_ShouldReturnEmpty() {
        Optional<Product> found = productRepository.findById(99999L);

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("根据关键字搜索 - 应返回匹配的商品")
    void searchByKeyword_ShouldReturnMatchingProducts() {
        Page<Product> result = productRepository.searchByKeyword("测试", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).contains("测试");
    }

    @Test
    @DisplayName("根据分类查询 - 应返回该分类的商品")
    void findByCategory_ShouldReturnProductsInCategory() {
        Page<Product> result = productRepository.findByCategory("电子产品", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategory()).isEqualTo("电子产品");
    }

    @Test
    @DisplayName("分页查询 - 应正确分页")
    void findAll_WithPagination_ShouldReturnPagedResults() {
        // 添加更多商品
        for (int i = 0; i < 15; i++) {
            Product p = new Product();
            p.setName("商品" + i);
            p.setPrice(new BigDecimal("10.00"));
            p.setStock(10);
            productRepository.save(p);
        }

        Page<Product> page1 = productRepository.findAll(PageRequest.of(0, 10));
        Page<Product> page2 = productRepository.findAll(PageRequest.of(1, 10));

        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(6); // 15 + 1(testProduct) - 10 = 6
        assertThat(page1.getTotalElements()).isEqualTo(16);
    }

    @Test
    @DisplayName("更新商品 - 应成功更新")
    void update_ShouldModifyProduct() {
        testProduct.setName("更新后的商品名");
        testProduct.setPrice(new BigDecimal("199.99"));
        productRepository.save(testProduct);

        Product updated = productRepository.findById(testProduct.getId()).orElseThrow();

        assertThat(updated.getName()).isEqualTo("更新后的商品名");
        assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("199.99"));
    }

    @Test
    @DisplayName("删除商品 - 应成功删除")
    void delete_ShouldRemoveProduct() {
        Long id = testProduct.getId();
        productRepository.deleteById(id);

        Optional<Product> deleted = productRepository.findById(id);

        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("统计商品数量 - 应返回正确数量")
    void count_ShouldReturnCorrectCount() {
        long count = productRepository.count();

        assertThat(count).isEqualTo(1);
    }
}
