#!/bin/bash
# 构建脚本

set -e

echo "=========================================="
echo "  电商数据管理系统 - 构建脚本"
echo "=========================================="

# 构建所有镜像
echo "📦 构建 Docker 镜像..."
docker compose build --no-cache

echo ""
echo "✅ 构建完成!"
echo ""
echo "镜像列表:"
docker images | grep ecommerce
