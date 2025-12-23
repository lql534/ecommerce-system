#!/bin/bash
# 测试脚本

set -e

echo "=========================================="
echo "  电商数据管理系统 - 运行测试"
echo "=========================================="

cd backend

echo "🧪 运行单元测试..."
mvn test -B

echo ""
echo "📊 生成测试报告..."
mvn jacoco:report

echo ""
echo "✅ 测试完成!"
echo "测试报告: backend/target/site/jacoco/index.html"
