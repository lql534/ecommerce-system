package com.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 255, message = "商品名称不能超过255个字符")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.00", message = "价格不能为负数")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    @Column(nullable = false)
    private Integer stock;

    @Size(max = 100, message = "分类名称不能超过100个字符")
    private String category;

    @Size(max = 500, message = "图片URL不能超过500个字符")
    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Integer status = 1; // 0-下架, 1-上架

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
