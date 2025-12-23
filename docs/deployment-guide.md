# 部署和运行指南

## 1. 环境要求

- Docker 20.10+
- Docker Compose 2.0+
- 内存: 4GB+
- 磁盘: 10GB+

## 2. 快速部署

### 2.1 克隆项目
```bash
git clone <repository-url>
cd ecommerce-system
```

### 2.2 配置环境变量（可选）
```bash
# 编辑 .env 文件
cp .env.example .env
vim .env
```

### 2.3 构建并启动
```bash
# 构建镜像并启动服务
docker compose up -d --build

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 2.4 访问服务
- 前端页面: http://localhost:80
- 后端API: http://localhost:8080/api
- Swagger文档: http://localhost:8080/swagger-ui.html
- phpMyAdmin: http://localhost:8081

## 3. 服务管理

### 3.1 启动/停止服务
```bash
# 启动
docker compose up -d

# 停止
docker compose down

# 停止并删除数据卷
docker compose down -v
```

### 3.2 重启单个服务
```bash
docker compose restart backend
docker compose restart frontend
```

### 3.3 查看日志
```bash
# 所有服务日志
docker compose logs -f

# 单个服务日志
docker compose logs -f backend
```

### 3.4 进入容器
```bash
docker exec -it ecommerce-backend sh
docker exec -it ecommerce-mysql mysql -u root -p
```

## 4. 数据库管理

### 4.1 备份数据库
```bash
docker exec ecommerce-mysql mysqldump -u root -proot123456 ecommerce > backup.sql
```

### 4.2 恢复数据库
```bash
docker exec -i ecommerce-mysql mysql -u root -proot123456 ecommerce < backup.sql
```

## 5. 监控部署

### 5.1 启动监控服务
```bash
cd monitoring
docker compose -f docker-compose.monitoring.yml up -d
```

### 5.2 访问监控
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin123)
- SkyWalking: http://localhost:8088

## 6. Jenkins CI/CD 部署

### 6.1 启动 Jenkins
```bash
cd jenkins
docker compose -f docker-compose.jenkins.yml up -d
```

### 6.2 获取初始密码
```bash
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

### 6.3 访问 Jenkins
- Jenkins: http://localhost:8082

## 7. Kubernetes 部署

### 7.1 部署到 K8s
```bash
# 创建命名空间
kubectl apply -f k8s/namespace.yaml

# 部署配置
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# 部署服务
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/ingress.yaml
```

### 7.2 查看部署状态
```bash
kubectl get all -n ecommerce
```

## 8. 生产环境建议

1. **安全配置**
   - 修改默认密码
   - 使用 HTTPS
   - 配置防火墙规则

2. **性能优化**
   - 调整 JVM 参数
   - 配置数据库连接池
   - 启用 Nginx 缓存

3. **高可用**
   - 配置数据库主从
   - 后端服务多副本
   - 使用负载均衡
