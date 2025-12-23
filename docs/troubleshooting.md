# 故障排查文档

## 1. 常见问题

### 1.1 容器启动失败

**问题**: 容器无法启动或立即退出

**排查步骤**:
```bash
# 查看容器状态
docker compose ps -a

# 查看容器日志
docker compose logs <service-name>

# 查看详细错误
docker inspect <container-id>
```

**常见原因**:
- 端口被占用
- 内存不足
- 配置文件错误

### 1.2 数据库连接失败

**问题**: 后端无法连接 MySQL

**排查步骤**:
```bash
# 检查 MySQL 是否运行
docker compose ps mysql

# 测试数据库连接
docker exec -it ecommerce-mysql mysql -u ecommerce_user -p

# 检查网络连通性
docker exec ecommerce-backend ping mysql
```

**解决方案**:
1. 确保 MySQL 健康检查通过
2. 检查数据库用户名密码
3. 确认服务在同一网络

### 1.3 前端无法访问后端 API

**问题**: 前端页面加载但 API 请求失败

**排查步骤**:
```bash
# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查 Nginx 配置
docker exec ecommerce-frontend cat /etc/nginx/nginx.conf

# 查看 Nginx 错误日志
docker exec ecommerce-frontend cat /var/log/nginx/error.log
```

**解决方案**:
1. 确认后端服务正常运行
2. 检查 Nginx 代理配置
3. 确认网络配置正确

### 1.4 镜像构建失败

**问题**: docker build 失败

**排查步骤**:
```bash
# 查看构建日志
docker build --no-cache -t test ./backend

# 检查 Dockerfile 语法
docker build --check ./backend
```

**常见原因**:
- 网络问题（无法下载依赖）
- Dockerfile 语法错误
- 基础镜像不存在

## 2. 性能问题

### 2.1 响应缓慢

**排查步骤**:
```bash
# 查看容器资源使用
docker stats

# 查看后端日志
docker compose logs -f backend | grep -i slow

# 检查数据库慢查询
docker exec ecommerce-mysql cat /var/log/mysql/slow.log
```

**优化建议**:
1. 增加 JVM 内存
2. 优化数据库查询
3. 启用缓存

### 2.2 内存不足

**排查步骤**:
```bash
# 查看内存使用
docker stats --no-stream

# 查看系统内存
free -h
```

**解决方案**:
```yaml
# docker-compose.yml 中限制内存
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 512M
```

## 3. 网络问题

### 3.1 容器间无法通信

**排查步骤**:
```bash
# 查看网络
docker network ls
docker network inspect ecommerce-system_ecommerce-network

# 测试连通性
docker exec ecommerce-backend ping mysql
docker exec ecommerce-frontend ping backend
```

**解决方案**:
1. 确保服务在同一网络
2. 使用服务名而非 IP
3. 检查防火墙规则

### 3.2 端口冲突

**排查步骤**:
```bash
# 查看端口占用
netstat -tlnp | grep 80
netstat -tlnp | grep 8080
netstat -tlnp | grep 3306
```

**解决方案**:
1. 停止占用端口的进程
2. 修改 docker-compose.yml 中的端口映射

## 4. 数据问题

### 4.1 数据丢失

**预防措施**:
```bash
# 使用命名卷
volumes:
  mysql_data:
    driver: local

# 定期备份
docker exec ecommerce-mysql mysqldump -u root -p ecommerce > backup_$(date +%Y%m%d).sql
```

### 4.2 数据库初始化失败

**排查步骤**:
```bash
# 查看初始化日志
docker compose logs mysql | grep -i error

# 检查初始化脚本
cat database/init.sql
```

**解决方案**:
1. 删除数据卷重新初始化
2. 检查 SQL 语法
3. 确认字符集配置

## 5. 日志收集

### 5.1 收集所有日志
```bash
# 导出日志
docker compose logs > logs_$(date +%Y%m%d).txt

# 导出单个服务日志
docker compose logs backend > backend_logs.txt
```

### 5.2 实时监控
```bash
# 实时查看所有日志
docker compose logs -f

# 只看错误
docker compose logs -f 2>&1 | grep -i error
```

## 6. 紧急恢复

### 6.1 服务完全重启
```bash
# 停止所有服务
docker compose down

# 清理未使用资源
docker system prune -f

# 重新启动
docker compose up -d --build
```

### 6.2 数据库恢复
```bash
# 停止服务
docker compose stop backend frontend

# 恢复数据
docker exec -i ecommerce-mysql mysql -u root -proot123456 ecommerce < backup.sql

# 重启服务
docker compose start backend frontend
```
