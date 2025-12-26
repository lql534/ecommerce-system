# 部署和运行指南

## 一、环境要求

### 1.1 硬件要求
| 资源 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 磁盘 | 20GB | 50GB |

### 1.2 软件要求
| 软件 | 版本 | 说明 |
|------|------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | 2.0+ | 容器编排 |
| Git | 2.0+ | 版本控制 |

### 1.3 检查环境
```bash
# 检查Docker版本
docker --version
# Docker version 26.1.4

# 检查Docker Compose版本
docker compose version
# Docker Compose version v2.27.1

# 检查Docker服务状态
systemctl status docker
```

## 二、快速部署

### 2.1 获取代码
```bash
# 方式1: 从GitHub克隆
git clone https://github.com/lql534/ecommerce-system.git
cd ecommerce-system

# 方式2: 直接使用本地代码
cd /root/ecommerce-system
```

### 2.2 一键启动
```bash
# 启动所有服务
docker compose up -d

# 查看服务状态
docker compose ps

# 查看日志
docker compose logs -f
```

### 2.3 验证部署
```bash
# 检查容器状态
docker ps

# 测试前端
curl http://localhost:80

# 测试后端API
curl http://localhost:8080/api/products

# 测试数据库连接
docker exec ecommerce-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

## 三、详细部署步骤

### 3.1 步骤1: 准备环境变量
```bash
# 查看默认环境变量
cat .env

# 内容如下:
# MYSQL_ROOT_PASSWORD=root123456
# MYSQL_DATABASE=ecommerce
# MYSQL_USER=ecommerce_user
# MYSQL_PASSWORD=ecommerce123
# SPRING_PROFILES_ACTIVE=prod

# 如需修改，编辑.env文件
vi .env
```

### 3.2 步骤2: 构建镜像
```bash
# 构建所有镜像
docker compose build

# 或单独构建
docker compose build frontend
docker compose build backend
```

### 3.3 步骤3: 启动数据库
```bash
# 先启动MySQL
docker compose up -d mysql

# 等待数据库就绪（约30秒）
sleep 30

# 检查数据库状态
docker compose ps mysql
# 状态应为 healthy
```

### 3.4 步骤4: 启动后端服务
```bash
# 启动后端
docker compose up -d backend

# 等待后端启动（约60秒）
sleep 60

# 检查后端状态
docker compose ps backend

# 查看后端日志
docker compose logs backend
```

### 3.5 步骤5: 启动前端服务
```bash
# 启动前端
docker compose up -d frontend

# 检查前端状态
docker compose ps frontend
```

### 3.6 步骤6: 启动管理工具
```bash
# 启动phpMyAdmin
docker compose up -d phpmyadmin
```

## 四、服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 前端页面 | http://192.168.19.129:80 | 商城首页 |
| 后端API | http://192.168.19.129:8080/api | RESTful接口 |
| phpMyAdmin | http://192.168.19.129:8081 | 数据库管理 |
| Jenkins | http://192.168.19.129:8082 | CI/CD控制台 |
| Registry | http://192.168.19.129:5000 | 私有镜像仓库 |

## 五、测试账号

| 角色 | 用户名 | 密码 | 权限 |
|------|--------|------|------|
| 管理员 | admin | admin123 | 商品CRUD、订单管理 |
| 普通用户 | user1 | user123 | 浏览、购物、下单 |

## 六、常用运维命令

### 6.1 服务管理
```bash
# 启动所有服务
docker compose up -d

# 停止所有服务
docker compose down

# 重启单个服务
docker compose restart backend

# 查看服务状态
docker compose ps

# 查看服务日志
docker compose logs -f backend
docker compose logs -f --tail=100 backend
```

### 6.2 镜像管理
```bash
# 重新构建镜像
docker compose build --no-cache

# 拉取最新镜像
docker compose pull

# 查看镜像列表
docker images | grep ecommerce

# 清理无用镜像
docker image prune -f
```

### 6.3 数据管理
```bash
# 查看数据卷
docker volume ls

# 备份数据库
docker exec ecommerce-mysql mysqldump -uroot -proot123456 ecommerce > backup.sql

# 恢复数据库
docker exec -i ecommerce-mysql mysql -uroot -proot123456 ecommerce < backup.sql

# 清理数据卷（危险！会删除数据）
docker compose down -v
```

### 6.4 网络管理
```bash
# 查看网络
docker network ls

# 查看网络详情
docker network inspect ecommerce-system_ecommerce-network

# 查看容器IP
docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' ecommerce-backend
```

## 七、Jenkins CI/CD 部署

### 7.1 启动Jenkins
```bash
cd jenkins
docker compose -f docker-compose.jenkins.yml up -d
```

### 7.2 访问Jenkins
1. 打开浏览器访问 http://192.168.19.129:8082
2. 创建Pipeline任务
3. 配置Pipeline脚本
4. 点击Build Now执行构建

### 7.3 自动化部署流程
```
代码修改 → 触发构建 → 单元测试 → 构建镜像 → 推送仓库 → 部署应用
```

## 八、Kubernetes 部署

### 8.1 前置条件
- 已安装Kubernetes集群
- 已配置kubectl

### 8.2 部署步骤
```bash
# 创建命名空间
kubectl apply -f k8s/namespace.yaml

# 创建配置
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# 部署MySQL
kubectl apply -f k8s/mysql-deployment.yaml

# 部署后端
kubectl apply -f k8s/backend-deployment.yaml

# 部署前端
kubectl apply -f k8s/frontend-deployment.yaml

# 配置Ingress
kubectl apply -f k8s/ingress.yaml
```

### 8.3 蓝绿部署
```bash
# 部署蓝色版本
kubectl apply -f k8s/blue-green/blue-deployment.yaml

# 部署绿色版本
kubectl apply -f k8s/blue-green/green-deployment.yaml

# 切换流量
kubectl apply -f k8s/blue-green/service.yaml
```

## 九、监控部署

### 9.1 启动监控服务
```bash
cd monitoring
docker compose -f docker-compose.monitoring.yml up -d
```

### 9.2 访问监控
| 服务 | 地址 | 说明 |
|------|------|------|
| Prometheus | http://192.168.19.129:9090 | 指标采集 |
| Grafana | http://192.168.19.129:3000 | 可视化面板 |

### 9.3 Grafana默认账号
- 用户名: admin
- 密码: admin

## 十、生产环境注意事项

### 10.1 安全配置
- [ ] 修改默认密码
- [ ] 配置HTTPS
- [ ] 限制端口访问
- [ ] 启用防火墙

### 10.2 性能优化
- [ ] 配置JVM参数
- [ ] 启用Nginx缓存
- [ ] 配置数据库连接池
- [ ] 启用Gzip压缩

### 10.3 高可用配置
- [ ] 配置数据库主从
- [ ] 配置负载均衡
- [ ] 配置健康检查
- [ ] 配置自动重启

### 10.4 备份策略
- [ ] 定时备份数据库
- [ ] 备份配置文件
- [ ] 备份日志文件
- [ ] 异地备份
