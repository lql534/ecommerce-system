package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 商品服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 获取所有商品（分页）
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        log.info("获取商品列表, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return productRepository.findAll(pageable);
    }

    /**
     * 根据ID获取商品
     */
    public Optional<Product> getProductById(Long id) {
        log.info("获取商品详情, id: {}", id);
        return productRepository.findById(id);
    }

    /**
     * 创建商品
     */
    @Transactional
    public Product createProduct(Product product) {
        log.info("创建商品: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * 更新商品
     */
    @Transactional
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        log.info("更新商品, id: {}", id);
        return productRepository.findById(id)
                .map(product -> {
                    product.setName(productDetails.getName());
                    product.setDescription(productDetails.getDescription());
                    product.setPrice(productDetails.getPrice());
                    product.setStock(productDetails.getStock());
                    product.setCategory(productDetails.getCategory());
                    product.setImageUrl(productDetails.getImageUrl());
                    if (productDetails.getStatus() != null) {
                        product.setStatus(productDetails.getStatus());
                    }
                    return productRepository.save(product);
                });
    }

    /**
     * 删除商品
     */
    @Transactional
    public boolean deleteProduct(Long id) {
        log.info("删除商品, id: {}", id);
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * 根据分类获取商品
     */
    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        log.info("根据分类获取商品: {}", category);
        return productRepository.findByCategory(category, pageable);
    }

    /**
     * 搜索商品
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        log.info("搜索商品: {}", keyword);
        return productRepository.searchByKeyword(keyword, pageable);
    }

    /**
     * 根据分类和关键词搜索
     */
    public Page<Product> searchProducts(String category, String keyword, Pageable pageable) {
        log.info("搜索商品, category: {}, keyword: {}", category, keyword);
        if (category != null && !category.isEmpty() && keyword != null && !keyword.isEmpty()) {
            return productRepository.searchByCategoryAndKeyword(category, keyword, pageable);
        } else if (category != null && !category.isEmpty()) {
            return productRepository.findByCategory(category, pageable);
        } else if (keyword != null && !keyword.isEmpty()) {
            return productRepository.searchByKeyword(keyword, pageable);
        }
        return productRepository.findAll(pageable);
    }

    /**
     * 获取库存不足的商品
     */
    public List<Product> getLowStockProducts(Integer threshold) {
        log.info("获取库存不足商品, threshold: {}", threshold);
        return productRepository.findByStockLessThan(threshold);
    }

    /**
     * 统计各分类商品数量
     */
    public List<Object[]> getCategoryStatistics() {
        return productRepository.countByCategory();
    }
}
