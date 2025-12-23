# 电商数据管理系统 (E-Commerce Data Management System)

基于Docker容器化技术的电商数据管理系统，包含前端服务、后端API服务、数据库服务，以及完整的CI/CD流水线。

## 项目架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend  │────▶│   Backend   │────▶│    MySQL    │
│   (Nginx)   │     │ (Spring Boot)│     │  Database   │
│   :80       │     │   :8080     │     │   :3306     │
└─────────────┘     └─────────────┘     └─────────────┘
       │                   │                   │
       └───────────────────┴───────────────────┘
                           │
                    ┌──────┴──────┐
                    │  Docker     │
                    │  Network    │
                    │ (ecommerce) │
                    └─────────────┘
```

## 技术栈

- **前端**: Nginx + HTML/CSS/JavaScript
- **后端**: Spring Boot 3.x + Java 21
- **数据库**: MySQL 8.0
- **容器编排**: Docker Compose / Kubernetes
- **CI/CD**: Jenkins
- **监控**: Prometheus + Grafana + SkyWalking

## 快速开始

### 1. 构建并启动服务

```bash
cd ecommerce-system
docker compose up -d --build
```

### 2. 访问服务

- 前端页面: http://localhost:80
- 后端API: http://localhost:8080/api
- phpMyAdmin: http://localhost:8081

### 3. 停止服务

```bash
docker compose down
```

## 项目结构

```
ecommerce-system/
├── frontend/                 # 前端服务
│   ├── Dockerfile
│   ├── nginx.conf
│   └── html/
├── backend/                  # 后端API服务
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
├── database/                 # 数据库服务
│   ├── Dockerfile
│   ├── init.sql
│   └── my.cnf
├── jenkins/                  # CI/CD配置
│   └── Jenkinsfile
├── k8s/                      # Kubernetes配置
├── monitoring/               # 监控配置
├── docker-compose.yml
└── docs/                     # 文档
```

## API接口

| 方法   | 路径                | 描述         |
|--------|---------------------|--------------|
| GET    | /api/products       | 获取商品列表 |
| GET    | /api/products/{id}  | 获取商品详情 |
| POST   | /api/products       | 创建商品     |
| PUT    | /api/products/{id}  | 更新商品     |
| DELETE | /api/products/{id}  | 删除商品     |

## 作者

Docker期末综合项目
