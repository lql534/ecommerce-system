# 代码规范文档

## 一、Java 代码规范

### 1.1 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰 | `ProductService`, `OrderController` |
| 方法名 | 小驼峰 | `getProductById`, `createOrder` |
| 变量名 | 小驼峰 | `productName`, `totalAmount` |
| 常量 | 全大写下划线 | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| 包名 | 全小写 | `com.ecommerce.service` |

### 1.2 代码示例

```java
/**
 * 商品服务类
 * 提供商品的CRUD操作
 * 
 * @author ecommerce-team
 * @version 1.0
 */
@Service
@Transactional
public class ProductService {
    
    private static final int DEFAULT_PAGE_SIZE = 10;
    
    private final ProductRepository productRepository;
    
    // 构造器注入（推荐）
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * 根据ID获取商品
     * 
     * @param id 商品ID
     * @return 商品实体
     * @throws ResourceNotFoundException 商品不存在时抛出
     */
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品不存在: " + id));
    }
}
```


### 1.3 注释规范

```java
/**
 * 类注释：说明类的用途
 */
public class Example {
    
    /** 字段注释：说明字段含义 */
    private String name;
    
    /**
     * 方法注释
     * @param param 参数说明
     * @return 返回值说明
     * @throws Exception 异常说明
     */
    public String method(String param) throws Exception {
        // 单行注释：解释复杂逻辑
        return param;
    }
}
```

### 1.4 Controller 规范

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(productService.createProduct(product));
    }
}
```

---

## 二、前端代码规范

### 2.1 JavaScript 规范

```javascript
/**
 * API服务模块
 * 封装所有后端API调用
 */
var api = {
    /**
     * 获取商品列表
     * @param {Object} params - 查询参数
     * @returns {Promise} 商品列表
     */
    getProducts: function(params) {
        var url = API_BASE_URL + '/products';
        return fetch(url)
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('请求失败');
                }
                return response.json();
            });
    }
};
```

### 2.2 HTML 规范

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>页面标题</title>
    <!-- 样式文件 -->
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <!-- 页面头部 -->
    <header class="header">
        <h1>标题</h1>
    </header>
    
    <!-- 主要内容 -->
    <main class="main">
        <section class="section">
            <h2>章节标题</h2>
            <p>内容</p>
        </section>
    </main>
    
    <!-- 页面底部 -->
    <footer class="footer">
        <p>版权信息</p>
    </footer>
    
    <!-- JavaScript文件 -->
    <script src="js/api.js"></script>
    <script src="js/app.js"></script>
</body>
</html>
```

### 2.3 CSS 规范

```css
/* ========================================
   样式文件说明
   ======================================== */

/* ---------- 变量定义 ---------- */
:root {
    --primary-color: #007bff;
    --text-color: #333;
    --bg-color: #f5f5f5;
}

/* ---------- 基础样式 ---------- */
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

body {
    font-family: Arial, sans-serif;
    color: var(--text-color);
    background-color: var(--bg-color);
}

/* ---------- 组件样式 ---------- */
.btn {
    padding: 10px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
}

.btn-primary {
    background-color: var(--primary-color);
    color: white;
}
```

---

## 三、配置文件规范

### 3.1 YAML 配置规范

```yaml
# application.yml - Spring Boot配置文件
# 使用注释说明每个配置项的用途

spring:
  # 应用名称
  application:
    name: ecommerce-backend
  
  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce
    username: ${DB_USER:root}      # 支持环境变量
    password: ${DB_PASSWORD:root}
    
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

# 服务器配置
server:
  port: 8080

# 日志配置
logging:
  level:
    root: INFO
    com.ecommerce: DEBUG
```

### 3.2 Docker Compose 规范

```yaml
# docker-compose.yml
# 服务编排配置文件

version: '3.8'

services:
  # 后端服务
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: ecommerce-backend
    restart: unless-stopped
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    ports:
      - "8080:8080"
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

networks:
  app-network:
    driver: bridge

volumes:
  mysql_data:
```

---

## 四、Dockerfile 最佳实践

### 4.1 规范示例

```dockerfile
# ============================================
# Dockerfile 规范示例
# ============================================

# 1. 使用官方基础镜像，指定具体版本
FROM nginx:1.25-alpine

# 2. 添加元数据标签
LABEL maintainer="team@example.com"
LABEL version="1.0"
LABEL description="Application description"

# 3. 设置环境变量
ENV TZ=Asia/Shanghai
ENV APP_HOME=/app

# 4. 安装依赖（合并RUN减少层数）
RUN apk add --no-cache \
    wget \
    curl \
    && rm -rf /var/cache/apk/*

# 5. 创建非root用户
RUN addgroup -g 1000 appgroup \
    && adduser -u 1000 -G appgroup -D appuser

# 6. 设置工作目录
WORKDIR $APP_HOME

# 7. 复制文件（利用缓存，先复制不常变的文件）
COPY package.json .
COPY src/ ./src/

# 8. 设置权限
RUN chown -R appuser:appgroup $APP_HOME

# 9. 切换用户
USER appuser

# 10. 暴露端口
EXPOSE 8080

# 11. 健康检查
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# 12. 启动命令
CMD ["./start.sh"]
```

---

## 五、代码质量检查

### 5.1 Checkstyle 配置

项目使用Maven Checkstyle插件进行代码规范检查：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <configuration>
        <configLocation>checkstyle.xml</configLocation>
    </configuration>
</plugin>
```

### 5.2 运行检查

```bash
# 运行代码规范检查
mvn checkstyle:check

# 生成检查报告
mvn checkstyle:checkstyle
```

---

## 六、总结

| 规范项 | 要求 |
|--------|------|
| 命名规范 | 遵循驼峰命名，见名知意 |
| 注释规范 | 类、方法、复杂逻辑必须注释 |
| 代码格式 | 统一缩进，合理换行 |
| 异常处理 | 不吞异常，合理处理 |
| 日志规范 | 使用日志框架，分级输出 |
| 配置管理 | 敏感信息使用环境变量 |
