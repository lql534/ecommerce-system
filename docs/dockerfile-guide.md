# Dockerfile 编写说明

## 一、Dockerfile 最佳实践

### 1.1 基础原则
- 使用官方基础镜像
- 选择轻量级镜像（alpine版本）
- 使用多阶段构建减小镜像体积
- 合理利用构建缓存
- 最小化镜像层数
- 使用非root用户运行

### 1.2 镜像大小优化
| 优化方式 | 说明 |
|----------|------|
| 多阶段构建 | 分离构建环境和运行环境 |
| Alpine镜像 | 使用轻量级基础镜像 |
| 清理缓存 | 删除包管理器缓存 |
| 合并RUN | 减少镜像层数 |

## 二、前端 Dockerfile 详解

```dockerfile
# ============================================
# 前端 Nginx Dockerfile
# 文件位置: frontend/Dockerfile
# ============================================

# ---------- 阶段1: 基础镜像 ----------
# 使用nginx:alpine作为基础镜像
# alpine版本体积小（约50MB vs 130MB）
FROM nginx:alpine

# ---------- 镜像元数据 ----------
# LABEL指令添加镜像元信息，便于管理和识别
LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce Frontend Service"
LABEL version="1.0"

# ---------- 时区配置 ----------
# 设置容器时区为上海时间
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ---------- 安装工具 ----------
# 安装wget用于健康检查
# --no-cache 不保存缓存，减小镜像体积
RUN apk add --no-cache wget

# ---------- 复制配置文件 ----------
# 复制自定义nginx配置
# 配置了反向代理、gzip压缩、缓存策略
COPY nginx.conf /etc/nginx/nginx.conf

# ---------- 复制静态文件 ----------
# 复制HTML、CSS、JS、图片等静态资源
COPY html/ /usr/share/nginx/html/

# ---------- 创建健康检查端点 ----------
# 创建/health端点用于容器健康检查
RUN echo "OK" > /usr/share/nginx/html/health

# ---------- 暴露端口 ----------
# 声明容器监听80端口
EXPOSE 80

# ---------- 健康检查 ----------
# 每30秒检查一次，超时10秒，启动等待10秒，最多重试3次
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget -q --spider http://localhost:80/health || exit 1

# ---------- 启动命令 ----------
# 使用nginx默认启动命令
# daemon off 让nginx在前台运行
CMD ["nginx", "-g", "daemon off;"]
```

### 前端镜像构建命令
```bash
# 构建镜像
docker build -t ecommerce-frontend:latest ./frontend

# 查看镜像大小
docker images | grep ecommerce-frontend
# 预期大小: 约 50-60MB
```

## 三、后端 Dockerfile 详解

```dockerfile
# ============================================
# 后端 Spring Boot Dockerfile (多阶段构建)
# 文件位置: backend/Dockerfile
# ============================================

# ========== 第一阶段: 构建阶段 ==========
# 使用Maven镜像进行编译打包
FROM maven:3.9-eclipse-temurin-21 AS builder

# 设置工作目录
WORKDIR /build

# ---------- 依赖缓存优化 ----------
# 先复制pom.xml，利用Docker缓存机制
# 只有pom.xml变化时才重新下载依赖
COPY pom.xml .

# 下载依赖（离线模式准备）
RUN mvn dependency:go-offline -B

# ---------- 复制源代码 ----------
COPY src ./src

# ---------- 编译打包 ----------
# -DskipTests 跳过测试加快构建
# -B 批处理模式，减少输出
RUN mvn clean package -DskipTests -B

# ========== 第二阶段: 运行阶段 ==========
# 使用JRE镜像，不需要完整JDK
FROM eclipse-temurin:21-jre-alpine

# ---------- 镜像元数据 ----------
LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce Backend API Service"
LABEL version="1.0"

# ---------- 时区配置 ----------
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ---------- 安装工具 ----------
# wget用于健康检查，curl用于调试
RUN apk add --no-cache wget curl

# ---------- 创建非root用户 ----------
# 安全最佳实践：不使用root运行应用
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

# ---------- 设置工作目录 ----------
WORKDIR /app

# ---------- 复制构建产物 ----------
# 从builder阶段复制JAR包
# 只复制最终产物，不包含源码和构建工具
COPY --from=builder /build/target/*.jar app.jar

# ---------- 设置文件权限 ----------
RUN chown -R appuser:appgroup /app

# ---------- 切换用户 ----------
USER appuser

# ---------- JVM配置 ----------
# 设置堆内存和GC策略
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# ---------- Spring配置 ----------
ENV SPRING_PROFILES_ACTIVE=prod

# ---------- 暴露端口 ----------
EXPOSE 8080

# ---------- 健康检查 ----------
# 检查Spring Boot Actuator健康端点
# start-period=120s 给应用足够的启动时间
HEALTHCHECK --interval=30s --timeout=15s --start-period=120s --retries=5 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# ---------- 启动命令 ----------
# 使用shell形式以支持环境变量展开
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 后端镜像构建命令
```bash
# 构建镜像
docker build -t ecommerce-backend:latest ./backend

# 查看镜像大小
docker images | grep ecommerce-backend
# 预期大小: 约 250-300MB (包含JRE)
```

### 多阶段构建效果对比
| 构建方式 | 镜像大小 | 说明 |
|----------|----------|------|
| 单阶段(JDK) | ~600MB | 包含完整JDK和Maven |
| 多阶段(JRE) | ~300MB | 仅包含JRE和应用 |
| 优化后 | ~250MB | 使用alpine基础镜像 |

## 四、数据库 Dockerfile 详解

```dockerfile
# ============================================
# MySQL 数据库 Dockerfile
# 文件位置: database/Dockerfile
# ============================================

# 使用官方MySQL 8.0镜像
FROM mysql:8.0

# ---------- 镜像元数据 ----------
LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce MySQL Database"
LABEL version="1.0"

# ---------- 时区配置 ----------
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# ---------- 复制配置文件 ----------
# 自定义MySQL配置
COPY my.cnf /etc/mysql/conf.d/my.cnf

# ---------- 复制初始化脚本 ----------
# docker-entrypoint-initdb.d目录下的脚本会在首次启动时自动执行
COPY init.sql /docker-entrypoint-initdb.d/

# ---------- 设置文件权限 ----------
RUN chmod 644 /etc/mysql/conf.d/my.cnf

# ---------- 暴露端口 ----------
EXPOSE 3306

# ---------- 健康检查 ----------
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
    CMD mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} || exit 1
```

## 五、Dockerfile 指令速查表

| 指令 | 说明 | 示例 |
|------|------|------|
| FROM | 指定基础镜像 | `FROM nginx:alpine` |
| LABEL | 添加元数据 | `LABEL version="1.0"` |
| ENV | 设置环境变量 | `ENV TZ=Asia/Shanghai` |
| RUN | 执行命令 | `RUN apk add --no-cache wget` |
| COPY | 复制文件 | `COPY src/ /app/` |
| ADD | 复制文件(支持URL和解压) | `ADD app.tar.gz /app/` |
| WORKDIR | 设置工作目录 | `WORKDIR /app` |
| EXPOSE | 声明端口 | `EXPOSE 8080` |
| USER | 切换用户 | `USER appuser` |
| HEALTHCHECK | 健康检查 | `HEALTHCHECK CMD curl -f http://localhost/` |
| ENTRYPOINT | 入口点 | `ENTRYPOINT ["java", "-jar", "app.jar"]` |
| CMD | 默认命令 | `CMD ["nginx", "-g", "daemon off;"]` |

## 六、构建缓存优化

### 6.1 缓存原理
Docker按层构建，每条指令生成一层。如果某层内容未变化，则使用缓存。

### 6.2 优化策略
```dockerfile
# ❌ 不好的写法 - 每次代码变化都重新下载依赖
COPY . /app
RUN mvn package

# ✅ 好的写法 - 依赖缓存
COPY pom.xml /app/
RUN mvn dependency:go-offline
COPY src /app/src
RUN mvn package
```

### 6.3 .dockerignore 文件
```
# 排除不需要的文件，加快构建速度
.git
.gitignore
*.md
target/
node_modules/
.idea/
*.log
```

## 七、安全最佳实践

### 7.1 使用非root用户
```dockerfile
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser
USER appuser
```

### 7.2 最小化安装
```dockerfile
# 只安装必要的包
RUN apk add --no-cache wget
```

### 7.3 固定版本
```dockerfile
# 使用具体版本而非latest
FROM nginx:1.25-alpine
FROM eclipse-temurin:21.0.1-jre-alpine
```

### 7.4 扫描漏洞
```bash
# 使用docker scan扫描镜像漏洞
docker scan ecommerce-backend:latest
```
