-- ============================================
-- 电商数据管理系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS ecommerce 
    DEFAULT CHARACTER SET utf8mb4 
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE ecommerce;

-- ============================================
-- 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    role VARCHAR(20) DEFAULT 'USER' COMMENT '角色: USER, ADMIN',
    status INT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 商品表
-- ============================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(255) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '商品价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    category VARCHAR(100) COMMENT '商品分类',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    status INT NOT NULL DEFAULT 1 COMMENT '状态: 0-下架, 1-上架',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================
-- 购物车表
-- ============================================
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '购物车项ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id),
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================
-- 订单表
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '订单状态: PENDING-待付款, PAID-已付款, SHIPPED-已发货, DELIVERED-已送达, CANCELLED-已取消',
    shipping_address VARCHAR(500) COMMENT '收货地址',
    remark VARCHAR(500) COMMENT '备注',
    paid_at TIMESTAMP NULL COMMENT '支付时间',
    shipped_at TIMESTAMP NULL COMMENT '发货时间',
    delivered_at TIMESTAMP NULL COMMENT '送达时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================
-- 订单明细表
-- ============================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订单明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称（快照）',
    product_price DECIMAL(10, 2) NOT NULL COMMENT '商品单价（快照）',
    quantity INT NOT NULL COMMENT '购买数量',
    subtotal DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ============================================
-- 插入管理员和测试用户
-- ============================================
INSERT INTO users (username, password, email, role) VALUES
    ('admin', 'admin123', 'admin@example.com', 'ADMIN'),
    ('user1', 'user123', 'user1@example.com', 'USER'),
    ('user2', 'user123', 'user2@example.com', 'USER')
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- ============================================
-- 插入示例商品数据（图片使用本地路径）
-- ============================================
INSERT INTO products (name, description, price, stock, category, image_url) VALUES
    ('iPhone 15 Pro', '苹果最新旗舰手机，A17 Pro芯片，钛金属设计', 8999.00, 100, '电子产品', '/images/iPhone 15 Pro.jpg'),
    ('MacBook Pro 14', 'M3 Pro芯片，14英寸Liquid视网膜XDR显示屏', 16999.00, 50, '电子产品', '/images/MacBook Pro 14.jpg'),
    ('AirPods Pro 2', '主动降噪，自适应音频，MagSafe充电盒', 1899.00, 200, '电子产品', '/images/AirPods Pro 2.jpg'),
    ('华为Mate 60 Pro', '麒麟9000S芯片，卫星通话，超可靠玄武架构', 6999.00, 80, '电子产品', '/images/华为Mate 60 Pro.jpg'),
    ('小米14 Ultra', '徕卡光学镜头，骁龙8 Gen3，专业影像旗舰', 5999.00, 120, '电子产品', '/images/小米14 Ultra.jpg'),
    ('男士休闲夹克', '春秋款纯棉外套，舒适透气，多色可选', 299.00, 500, '服装', '/images/男士休闲夹克.jpg'),
    ('女士连衣裙', '夏季新款碎花裙，优雅气质，修身显瘦', 199.00, 300, '服装', '/images/女士连衣裙.jpg'),
    ('运动T恤', '速干面料，透气排汗，健身跑步必备', 89.00, 800, '服装', '/images/运动T恤.jpg'),
    ('牛仔裤', '经典直筒版型，弹力舒适，百搭款式', 159.00, 400, '服装', '/images/牛仔裤.jpg'),
    ('进口坚果礼盒', '混合坚果，营养健康，送礼佳品', 128.00, 1000, '食品', '/images/进口坚果礼盒.jpg'),
    ('有机牛奶', '新鲜有机，营养丰富，1L装', 25.00, 2000, '食品', '/images/有机牛奶.jpg'),
    ('精品咖啡豆', '阿拉比卡咖啡豆，中度烘焙，香醇浓郁', 88.00, 500, '食品', '/images/精品咖啡豆.jpg'),
    ('北欧风沙发', '简约现代设计，高密度海绵，舒适耐用', 2999.00, 30, '家居', '/images/北欧风沙发.jpg'),
    ('智能台灯', 'LED护眼灯，无极调光，触控开关', 199.00, 600, '家居', '/images/智能台灯.jpg'),
    ('不锈钢锅具套装', '304不锈钢，电磁炉通用，5件套', 399.00, 200, '家居', '/images/不锈钢锅具套装.jpg')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- ============================================
-- 授权
-- ============================================
GRANT ALL PRIVILEGES ON ecommerce.* TO 'ecommerce_user'@'%';
FLUSH PRIVILEGES;

SELECT 'Database initialization completed!' AS Message;
