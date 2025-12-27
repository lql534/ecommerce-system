package com.ecommerce.service;

import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("购物车服务单元测试")
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("测试商品");
        testProduct.setPrice(new BigDecimal("99.99"));
        testProduct.setStock(100);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setUserId(1L);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
    }

    @Test
    @DisplayName("获取用户购物车列表")
    void getCartItems_ShouldReturnCartItems() {
        when(cartItemRepository.findByUserId(1L)).thenReturn(Arrays.asList(testCartItem));

        List<CartItem> result = cartService.getCartItems(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get(0).getQuantity());
    }

    @Test
    @DisplayName("添加商品到购物车 - 新商品")
    void addToCart_NewProduct_ShouldCreateCartItem() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem result = cartService.addToCart(1L, 1L, 2);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    @DisplayName("添加商品到购物车 - 已存在商品增加数量")
    void addToCart_ExistingProduct_ShouldIncreaseQuantity() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem result = cartService.addToCart(1L, 1L, 3);

        assertNotNull(result);
        assertEquals(5, testCartItem.getQuantity()); // 2 + 3
    }

    @Test
    @DisplayName("添加商品到购物车 - 库存不足")
    void addToCart_InsufficientStock_ShouldThrowException() {
        testProduct.setStock(1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(1L, 1L, 5);
        });
    }

    @Test
    @DisplayName("添加商品到购物车 - 商品不存在")
    void addToCart_ProductNotFound_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cartService.addToCart(1L, 999L, 1);
        });
    }

    @Test
    @DisplayName("更新购物车商品数量")
    void updateCartItem_ShouldUpdateQuantity() {
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem result = cartService.updateCartItem(1L, 1L, 5);

        assertNotNull(result);
        assertEquals(5, testCartItem.getQuantity());
    }

    @Test
    @DisplayName("更新购物车商品数量为0 - 删除商品")
    void updateCartItem_ZeroQuantity_ShouldRemoveItem() {
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));

        CartItem result = cartService.updateCartItem(1L, 1L, 0);

        assertNull(result);
        verify(cartItemRepository, times(1)).delete(testCartItem);
    }

    @Test
    @DisplayName("更新购物车 - 库存不足")
    void updateCartItem_InsufficientStock_ShouldThrowException() {
        testProduct.setStock(3);
        when(cartItemRepository.findByUserIdAndProductId(1L, 1L)).thenReturn(Optional.of(testCartItem));

        assertThrows(RuntimeException.class, () -> {
            cartService.updateCartItem(1L, 1L, 10);
        });
    }

    @Test
    @DisplayName("从购物车移除商品")
    void removeFromCart_ShouldDeleteItem() {
        doNothing().when(cartItemRepository).deleteByUserIdAndProductId(1L, 1L);

        cartService.removeFromCart(1L, 1L);

        verify(cartItemRepository, times(1)).deleteByUserIdAndProductId(1L, 1L);
    }

    @Test
    @DisplayName("清空购物车")
    void clearCart_ShouldDeleteAllItems() {
        doNothing().when(cartItemRepository).deleteByUserId(1L);

        cartService.clearCart(1L);

        verify(cartItemRepository, times(1)).deleteByUserId(1L);
    }
}
