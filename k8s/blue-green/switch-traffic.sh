#!/bin/bash
# 蓝绿部署流量切换脚本

NAMESPACE="ecommerce"
SERVICE_NAME="backend-bluegreen"

# 获取当前版本
CURRENT_VERSION=$(kubectl get svc $SERVICE_NAME -n $NAMESPACE -o jsonpath='{.spec.selector.version}')

echo "当前版本: $CURRENT_VERSION"

if [ "$CURRENT_VERSION" == "blue" ]; then
    NEW_VERSION="green"
else
    NEW_VERSION="blue"
fi

echo "切换到版本: $NEW_VERSION"

# 确认切换
read -p "确认切换流量到 $NEW_VERSION 版本? (y/n) " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]; then
    # 切换流量
    kubectl patch svc $SERVICE_NAME -n $NAMESPACE -p "{\"spec\":{\"selector\":{\"version\":\"$NEW_VERSION\"}}}"
    
    echo "✅ 流量已切换到 $NEW_VERSION 版本"
    
    # 验证切换
    kubectl get svc $SERVICE_NAME -n $NAMESPACE -o wide
else
    echo "❌ 取消切换"
fi
