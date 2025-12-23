package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品数据访问层
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 根据分类查询商品
     */
    List<Product> findByCategory(String category);

    /**
     * 根据分类分页查询商品
     */
    Page<Product> findByCategory(String category, Pageable pageable);

    /**
     * 根据状态查询商品
     */
    List<Product> findByStatus(Integer status);

    /**
     * 根据名称模糊搜索
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据分类和关键词搜索
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByCategoryAndKeyword(@Param("category") String category, 
                                              @Param("keyword") String keyword, 
                                              Pageable pageable);

    /**
     * 统计各分类商品数量
     */
    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();

    /**
     * 查询库存不足的商品
     */
    List<Product> findByStockLessThan(Integer threshold);
}
