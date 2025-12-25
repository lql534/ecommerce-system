# 电商数据管理系统

基于Docker容器化技术的电商数据管理系统，包含前端服务、后端API服务、数据库服务，并实现完整的CI/CD流水线。

## 项目架构

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   Nginx     │───▶│ Spring Boot │───▶│   MySQL     │
│  Frontend   │    │   Backend   │    │  Database   │
│    :80      │    │   :8080     │    │   :3306     │
└─────────────┘    └─────────────┘    └─────────────┘
```

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Nginx + HTML/CSS/JavaScript |
| 后端 | Spring Boot 3.2 + JPA |
| 数据库 | MySQL 8.0 |
| 容器化 | Docker + Docker Compose |
| CI/CD | Jenkins + GitHub Actions |
| 编排 | Kubernetes |
| 监控 | Prometheus + Grafana |

## 快速开始

```bash
# 克隆项目
git clone https://github.com/lql534/ecommerce-system.git
cd ecommerce-system

# 启动服务
docker compose up -d

# 查看状态
docker compose ps
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost:80 |
| 后端API | http://localhost:8080/api |
| phpMyAdmin | http://localhost:8081 |
| Jenkins | http://localhost:8082 |

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 普通用户 | user1 | user123 |

## 项目文档

| 文档 | 说明 |
|------|------|
| [架构说明](docs/architecture.md) | 系统架构、技术栈、目录结构 |
| [Dockerfile说明](docs/dockerfile-guide.md) | Dockerfile编写规范和最佳实践 |
| [部署指南](docs/deployment-guide.md) | 详细的部署和运行步骤 |
| [故障排查](docs/troubleshooting.md) | 常见问题及解决方案 |
| [CI/CD指南](docs/cicd-guide.md) | Jenkins流水线配置说明 |
| [Git协作规范](docs/git-workflow.md) | 分支策略、Commit规范、Code Review |
| [代码规范](docs/code-standards.md) | Java/前端/配置文件编码规范 |

## 目录结构

```
ecommerce-system/
├── backend/          # 后端Spring Boot服务
├── frontend/         # 前端Nginx服务
├── database/         # 数据库配置
├── jenkins/          # Jenkins CI/CD配置
├── k8s/              # Kubernetes部署配置
├── monitoring/       # 监控配置
├── docs/             # 项目文档
├── scripts/          # 运维脚本
└── docker-compose.yml
```

## 功能特性

- ✅ 商品管理（CRUD）
- ✅ 用户登录认证
- ✅ 购物车功能
- ✅ 订单管理
- ✅ Docker容器化部署
- ✅ CI/CD自动化流水线
- ✅ Kubernetes编排
- ✅ 蓝绿部署
- ✅ 监控告警

## License

MIT License
