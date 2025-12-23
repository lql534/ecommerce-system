# ============================================
# 前端服务 Dockerfile
# 基于 nginx:alpine 构建静态页面容器
# ============================================

# 使用 nginx alpine 版本（轻量级）
FROM nginx:alpine

# 维护者信息
LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce Frontend Service"
LABEL version="1.0"

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 安装 wget 用于健康检查
RUN apk add --no-cache wget

# 删除默认配置和页面
RUN rm -rf /etc/nginx/conf.d/default.conf /usr/share/nginx/html/*

# 复制自定义 nginx 配置
COPY nginx.conf /etc/nginx/nginx.conf

# 复制静态页面文件
COPY html/ /usr/share/nginx/html/

# 创建健康检查端点
RUN mkdir -p /usr/share/nginx/html/health && \
    echo "OK" > /usr/share/nginx/html/health/index.html

# 设置文件权限
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget -q --spider http://localhost:80/health || exit 1

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
