# ============================================
# MySQL数据库服务 Dockerfile
# 基于 mysql:8.0 构建
# ============================================

FROM mysql:8.0

# 维护者信息
LABEL maintainer="ecommerce-team"
LABEL description="E-Commerce MySQL Database Service"
LABEL version="1.0"

# 设置环境变量
ENV MYSQL_ROOT_PASSWORD=root123456
ENV MYSQL_DATABASE=ecommerce
ENV MYSQL_USER=ecommerce_user
ENV MYSQL_PASSWORD=ecommerce123
ENV TZ=Asia/Shanghai

# 设置时区
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制自定义配置文件
COPY my.cnf /etc/mysql/conf.d/my.cnf

# 复制初始化脚本
COPY init.sql /docker-entrypoint-initdb.d/

# 设置配置文件权限
RUN chmod 644 /etc/mysql/conf.d/my.cnf

# 暴露端口
EXPOSE 3306

# 健康检查
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
    CMD mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} || exit 1

# 使用默认的 entrypoint 和 cmd
