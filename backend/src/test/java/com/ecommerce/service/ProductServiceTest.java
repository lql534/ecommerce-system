package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("商品服务单元测试")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
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
    @DisplayName("获取所有商品 - 分页")
    void getAllProducts_ShouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<Product> result = productService.getAllProducts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("根据ID获取商品 - 存在")
    void getProductById_WhenExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        assertEquals("测试商品", result.get().getName());
    }

    @Test
    @DisplayName("根据ID获取商品 - 不存在")
    void getProductById_WhenNotExists_ShouldReturnEmpty() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("创建商品")
    void createProduct_ShouldReturnCreatedProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertNotNull(result);
        assertEquals("测试商品", result.getName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("更新商品 - 存在")
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() {
        Product updatedProduct = new Product();
        updatedProduct.setName("更新后的商品");
        updatedProduct.setPrice(new BigDecimal("199.99"));
        updatedProduct.setStock(50);
        updatedProduct.setCategory("服装");

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Optional<Product> result = productService.updateProduct(1L, updatedProduct);

        assertTrue(result.isPresent());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("更新商品 - 不存在")
    void updateProduct_WhenNotExists_ShouldReturnEmpty() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.updateProduct(999L, testProduct);

        assertFalse(result.isPresent());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("删除商品 - 存在")
    void deleteProduct_WhenExists_ShouldReturnTrue() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        boolean result = productService.deleteProduct(1L);

        assertTrue(result);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除商品 - 不存在")
    void deleteProduct_WhenNotExists_ShouldReturnFalse() {
        when(productRepository.existsById(999L)).thenReturn(false);

        boolean result = productService.deleteProduct(999L);

        assertFalse(result);
        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("根据分类获取商品")
    void getProductsByCategory_ShouldReturnProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByCategory("电子产品", pageable)).thenReturn(productPage);

        Page<Product> result = productService.getProductsByCategory("电子产品", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("搜索商品")
    void searchProducts_ShouldReturnMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(testProduct);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.searchByKeyword("测试", pageable)).thenReturn(productPage);

        Page<Product> result = productService.searchProducts("测试", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("获取库存不足商品")
    void getLowStockProducts_ShouldReturnLowStockProducts() {
        testProduct.setStock(5);
        List<Product> lowStockProducts = Arrays.asList(testProduct);

        when(productRepository.findByStockLessThan(10)).thenReturn(lowStockProducts);

        List<Product> result = productService.getLowStockProducts(10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getStock() < 10);
    }
}
