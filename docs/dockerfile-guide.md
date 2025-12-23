# Dockerfile 编写说明

## 1. 前端 Dockerfile 说明

```dockerfile
# 使用 alpine 版本减小镜像体积
FROM nginx:alpine

# 安装健康检查工具
RUN apk add --no-cache wget

# 复制配置和静态文件
COPY nginx.conf /etc/nginx/nginx.conf
COPY html/ /usr/share/nginx/html/

# 健康检查配置
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget -q --spider http://localhost:80/health || exit 1
```

### 最佳实践
- 使用 alpine 版本（约50MB vs 标准版130MB）
- 配置健康检查确保服务可用性
- 设置合理的文件权限

## 2. 后端 Dockerfile 说明

```dockerfile
# ===== 构建阶段 =====
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B  # 利用缓存
COPY src ./src
RUN mvn clean package -DskipTests -B

# ===== 运行阶段 =====
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

# 非root用户运行
RUN addgroup -g 1000 appgroup && adduser -u 1000 -G appgroup -D appuser
USER appuser

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 多阶段构建优势
- 构建镜像：~500MB（包含Maven和JDK）
- 运行镜像：~260MB（仅JRE）
- 减少约50%镜像体积

### 最佳实践
- 分离依赖下载和代码编译（利用Docker缓存）
- 使用JRE而非JDK运行
- 非root用户提高安全性
- 配置JVM参数优化内存使用

## 3. 数据库 Dockerfile 说明

```dockerfile
FROM mysql:8.0

# 复制配置文件
COPY my.cnf /etc/mysql/conf.d/my.cnf
COPY init.sql /docker-entrypoint-initdb.d/

# 健康检查
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
    CMD mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} || exit 1
```

### 最佳实践
- 使用官方镜像确保安全性
- 初始化脚本放在 `/docker-entrypoint-initdb.d/`
- 配置合理的健康检查参数

## 4. 镜像大小对比

| 镜像 | 优化前 | 优化后 | 减少 |
|------|--------|--------|------|
| Frontend | ~130MB | ~53MB | 59% |
| Backend | ~500MB | ~264MB | 47% |
| MySQL | ~781MB | ~781MB | - |

## 5. 构建命令

```bash
# 构建所有镜像
docker compose build

# 单独构建
docker build -t ecommerce-frontend ./frontend
docker build -t ecommerce-backend ./backend
docker build -t ecommerce-mysql ./database
```
