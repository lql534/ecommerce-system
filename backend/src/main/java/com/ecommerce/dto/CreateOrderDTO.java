package com.ecommerce.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderDTO {
    private Long userId;
    private String shippingAddress;
    private String remark;
    private List<OrderItemDTO> items; // 如果为空则从购物车创建
    
    @Data
    public static class OrderItemDTO {
        private Long productId;
        private Integer quantity;
    }
}
