package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品CRUD接口")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    @GetMapping
    @Operation(summary = "获取商品列表", description = "支持分页、搜索和分类筛选")
    public ResponseEntity<Page<Product>> getProducts(
            @Parameter(description = "页码") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "12") int size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商品分类") @RequestParam(required = false) String category,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "排序方向") @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products = productService.searchProducts(category, keyword, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据ID获取商品详细信息")
    public ResponseEntity<Product> getProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "创建商品", description = "创建新商品")
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "根据ID更新商品信息")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Valid @RequestBody Product product) {
        return productService.updateProduct(id, product)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "根据ID删除商品")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键词搜索商品")
    public ResponseEntity<Page<Product>> searchProducts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.searchProducts(keyword, pageable));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取商品", description = "根据分类获取商品列表")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @Parameter(description = "商品分类") @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(productService.getProductsByCategory(category, pageable));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "获取库存不足商品", description = "获取库存低于阈值的商品")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @Parameter(description = "库存阈值") @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }
    @GetMapping("/statistics/category")
    @Operation(summary = "获取分类统计", description = "统计各分类商品数量")
    public ResponseEntity<List<Object[]>> getCategoryStatistics() {
        return ResponseEntity.ok(productService.getCategoryStatistics());
    }
}
