# 电商数据管理系统

基于Docker的电商数据管理系统，包含完整的CI/CD流水线和Prometheus监控。

## 项目架构

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   前端      │────▶│   后端API   │────▶│   MySQL     │
│   Nginx     │     │ Spring Boot │     │   数据库    │
│   :80       │     │   :8080     │     │   :3306     │
└─────────────┘     └─────────────┘     └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │ Prometheus  │
                    │   监控      │
                    │   :9090     │
                    └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  Grafana    │
                    │  可视化     │
                    │   :3000     │
                    └─────────────┘
```

## 技术栈

| 组件 | 技术 | 版本 |
|------|------|------|
| 前端 | Nginx + HTML/CSS/JS | Alpine |
| 后端 | Spring Boot | 3.2.0 |
| 数据库 | MySQL | 8.0 |
| 容器化 | Docker | 24.x |
| CI/CD | Jenkins | LTS |
| 镜像仓库 | Harbor | 2.x |
| 监控 | Prometheus + Grafana | Latest |

## 功能模块

- 商品管理（CRUD）
- 用户登录/注册
- 购物车管理
- 订单管理
- 后台管理

## 快速启动

### 1. 启动应用
```bash
cd ecommerce-system
docker compose up -d
```

### 2. 启动监控
```bash
cd monitoring
docker compose -f docker-compose.monitoring.yml up -d
```

### 3. 访问地址
- 前端: http://192.168.19.129:80
- 后端API: http://192.168.19.129:8080/api
- Prometheus: http://192.168.19.129:9090
- Grafana: http://192.168.19.129:3000 (admin/admin123)
- Harbor: http://192.168.19.129:8083 (admin/Harbor12345)
- Jenkins: http://192.168.19.129:8082

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user1 | user123 |

## CI/CD流水线

Jenkins自动化流水线包含以下阶段：

1. **代码检出** - 从GitHub拉取代码
2. **项目验证** - 检查项目结构
3. **代码质量检查** - 检查代码规范
4. **单元测试** - Maven运行测试
5. **构建应用** - Maven打包JAR
6. **镜像构建** - 构建Docker镜像
7. **推送Harbor** - 推送到私有仓库
8. **集成测试** - 容器化测试
9. **自动部署** - 部署到服务器
10. **部署验证** - 验证服务状态

### 触发方式
- Poll SCM: 每分钟检查GitHub变化
- 手动触发: Jenkins点击Build Now

## 目录结构

```
ecommerce-system/
├── backend/                 # 后端Spring Boot项目
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # 前端Nginx项目
│   ├── html/
│   ├── nginx.conf
│   └── Dockerfile
├── database/                # 数据库配置
│   ├── init.sql
│   └── Dockerfile
├── jenkins/                 # Jenkins CI/CD配置
│   ├── Jenkinsfile
│   └── docker-compose.jenkins.yml
├── monitoring/              # Prometheus监控配置
│   ├── docker-compose.monitoring.yml
│   ├── prometheus/
│   └── grafana/
├── docs/                    # 技术文档
├── scripts/                 # 运维脚本
├── docker-compose.yml       # 主编排文件
└── README.md
```

## 文档

- [架构说明](docs/architecture.md)
- [Dockerfile说明](docs/dockerfile-guide.md)
- [部署指南](docs/deployment-guide.md)
- [故障排查](docs/troubleshooting.md)
- [CI/CD指南](docs/cicd-guide.md)
- [Git协作规范](docs/git-workflow.md)
- [代码规范](docs/code-standards.md)

## API接口

### 商品接口
- `GET /api/products` - 获取商品列表
- `GET /api/products/{id}` - 获取商品详情
- `POST /api/products` - 创建商品
- `PUT /api/products/{id}` - 更新商品
- `DELETE /api/products/{id}` - 删除商品

### 用户接口
- `POST /api/users/login` - 用户登录
- `POST /api/users/register` - 用户注册

### 购物车接口
- `GET /api/cart` - 获取购物车
- `POST /api/cart/add` - 添加商品
- `DELETE /api/cart/{id}` - 删除商品

### 订单接口
- `GET /api/orders` - 获取订单列表
- `POST /api/orders` - 创建订单
- `PUT /api/orders/{id}/status` - 更新订单状态
