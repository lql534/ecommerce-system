package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByCategory(String category);

    Page<Product> findByCategory(String category, Pageable pageable);

    List<Product> findByStatus(Integer status);

    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% OR p.description LIKE %:keyword%")
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND (p.name LIKE %:keyword% OR p.description LIKE %:keyword%)")
    Page<Product> searchByCategoryAndKeyword(@Param("category") String category, 
                                              @Param("keyword") String keyword, 
                                              Pageable pageable);

    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> countByCategory();

    List<Product> findByStockLessThan(Integer threshold);
}
