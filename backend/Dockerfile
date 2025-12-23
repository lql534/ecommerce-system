# ============================================
# 后端API服务 Dockerfile
# 多阶段构建优化镜像大小
# ============================================

# ==================== 构建阶段 ====================
FROM maven:3.9-eclipse-temurin-21 AS builder

# 设置工作目录
WORKDIR /build

# 复制 pom.xml 并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试以加快构建）
RUN mvn clean package -DskipTests -B

# ==================== 运行阶段 ====================
FROM eclipse-temurin:21-jre-alpine

# 维护者信息
LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce Backend API Service"
LABEL version="1.0"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装必要工具
RUN apk add --no-cache wget curl

# 创建非root用户
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /build/target/*.jar app.jar

# 设置文件权限
RUN chown -R appuser:appgroup /app

# 切换到非root用户
USER appuser

# 环境变量配置
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
ENV SPRING_PROFILES_ACTIVE=prod

# 暴露端口
EXPOSE 8080

# 健康检查（增加启动等待时间）
HEALTHCHECK --interval=30s --timeout=15s --start-period=120s --retries=5 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
