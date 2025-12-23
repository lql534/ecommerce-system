package com.ecommerce.controller;

import com.ecommerce.dto.CartItemDTO;
import com.ecommerce.entity.CartItem;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "购物车管理", description = "购物车CRUD接口")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartService cartService;

    @GetMapping("/{userId}")
    @Operation(summary = "获取购物车", description = "获取用户购物车列表")
    public ResponseEntity<List<CartItem>> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping("/{userId}")
    @Operation(summary = "添加到购物车", description = "添加商品到购物车")
    public ResponseEntity<?> addToCart(
            @PathVariable Long userId,
            @RequestBody CartItemDTO dto) {
        try {
            CartItem item = cartService.addToCart(userId, dto.getProductId(), dto.getQuantity());
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/{productId}")
    @Operation(summary = "更新购物车", description = "更新购物车商品数量")
    public ResponseEntity<?> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer quantity = body.get("quantity");
            CartItem item = cartService.updateCartItem(userId, productId, quantity);
            if (item == null) {
                return ResponseEntity.ok(Map.of("message", "商品已从购物车移除"));
            }
            return ResponseEntity.ok(item);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId}/{productId}")
    @Operation(summary = "移除商品", description = "从购物车移除商品")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(Map.of("message", "商品已移除"));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "清空购物车", description = "清空用户购物车")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(Map.of("message", "购物车已清空"));
    }
}
