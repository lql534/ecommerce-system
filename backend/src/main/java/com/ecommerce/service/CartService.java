package com.ecommerce.service;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    @Transactional
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足");
        }

        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("库存不足");
            }
            item.setQuantity(newQuantity);
            return cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            return cartItemRepository.save(newItem);
        }
    }

    @Transactional
    public CartItem updateCartItem(Long userId, Long productId, Integer quantity) {
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            return null;
        }

        Product product = item.getProduct();
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存: " + product.getStock());
        }

        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeFromCart(Long userId, Long productId) {
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
