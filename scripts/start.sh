#!/bin/bash
# 启动脚本

set -e

echo "=========================================="
echo "  电商数据管理系统 - 启动服务"
echo "=========================================="

# 启动服务
echo "🚀 启动服务..."
docker compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo ""
echo "📊 服务状态:"
docker compose ps

echo ""
echo "✅ 服务已启动!"
echo ""
echo "访问地址:"
echo "  - 前端页面: http://localhost:80"
echo "  - 后端API: http://localhost:8080/api"
echo "  - Swagger: http://localhost:8080/swagger-ui.html"
echo "  - phpMyAdmin: http://localhost:8081"
