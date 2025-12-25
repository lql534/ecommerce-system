# 故障排查文档

## 一、快速诊断命令

```bash
# 查看所有容器状态
docker ps -a

# 查看容器日志
docker logs <container_name>

# 进入容器调试
docker exec -it <container_name> sh

# 查看容器资源使用
docker stats

# 查看网络连接
docker network inspect ecommerce-system_ecommerce-network
```

## 二、常见问题及解决方案

### 问题1: 容器启动失败

#### 症状
```
docker compose up -d
Container ecommerce-backend exited with code 1
```

#### 排查步骤
```bash
# 1. 查看容器日志
docker logs ecommerce-backend

# 2. 查看详细错误
docker logs ecommerce-backend 2>&1 | tail -50

# 3. 检查镜像是否存在
docker images | grep ecommerce-backend
```

#### 常见原因及解决
| 原因 | 解决方案 |
|------|----------|
| 镜像不存在 | `docker compose build backend` |
| 端口被占用 | `lsof -i:8080` 然后 `kill <PID>` |
| 内存不足 | 增加服务器内存或调整JVM参数 |
| 配置错误 | 检查application.yml配置 |

---

### 问题2: 数据库连接失败

#### 症状
```
Connection refused to host: mysql:3306
Unable to acquire JDBC Connection
```

#### 排查步骤
```bash
# 1. 检查MySQL容器状态
docker ps | grep mysql
docker logs ecommerce-mysql

# 2. 检查MySQL是否健康
docker inspect ecommerce-mysql | grep -A 5 Health

# 3. 测试数据库连接
docker exec ecommerce-mysql mysql -uroot -proot123456 -e "SELECT 1"

# 4. 检查网络连通性
docker exec ecommerce-backend ping mysql
```

#### 常见原因及解决
| 原因 | 解决方案 |
|------|----------|
| MySQL未启动 | `docker compose up -d mysql` |
| MySQL未就绪 | 等待30秒后重试 |
| 密码错误 | 检查.env文件中的密码配置 |
| 网络不通 | 检查容器是否在同一网络 |

#### 解决命令
```bash
# 重启MySQL
docker compose restart mysql

# 等待MySQL就绪
sleep 30

# 重启后端
docker compose restart backend
```

---

### 问题3: 前端无法访问后端API

#### 症状
```
浏览器控制台显示:
Failed to fetch
CORS error
502 Bad Gateway
```

#### 排查步骤
```bash
# 1. 检查后端是否运行
curl http://localhost:8080/actuator/health

# 2. 检查Nginx配置
docker exec ecommerce-frontend cat /etc/nginx/nginx.conf

# 3. 检查Nginx日志
docker logs ecommerce-frontend

# 4. 测试API直接访问
curl http://localhost:8080/api/products
```

#### 常见原因及解决
| 原因 | 解决方案 |
|------|----------|
| 后端未启动 | `docker compose up -d backend` |
| Nginx配置错误 | 检查nginx.conf中的proxy_pass |
| 后端启动中 | 等待后端完全启动（约60秒） |
| 网络问题 | 检查容器网络配置 |

---

### 问题4: 页面显示旧内容（缓存问题）

#### 症状
```
修改了代码但页面没有更新
JavaScript报错：函数不存在
```

#### 解决方案
```bash
# 1. 强制重新构建镜像
docker compose build --no-cache frontend

# 2. 重启前端容器
docker compose up -d --force-recreate frontend

# 3. 清除浏览器缓存
# Chrome: Ctrl+Shift+Delete
# 或使用无痕模式访问
```

---

### 问题5: Docker网络错误

#### 症状
```
failed to create network: Error response from daemon
iptables failed: iptables --wait -t nat -I DOCKER
```

#### 解决方案
```bash
# 1. 停止所有容器
docker stop $(docker ps -aq)

# 2. 清理网络
docker network prune -f

# 3. 重启Docker服务
systemctl restart docker

# 4. 重新启动服务
docker compose up -d
```

---

### 问题6: 磁盘空间不足

#### 症状
```
no space left on device
write /var/lib/docker/...: no space left on device
```

#### 解决方案
```bash
# 1. 查看磁盘使用
df -h

# 2. 查看Docker占用
docker system df

# 3. 清理无用资源
docker system prune -a -f

# 4. 清理无用镜像
docker image prune -a -f

# 5. 清理无用卷
docker volume prune -f
```

---

### 问题7: 内存不足

#### 症状
```
Java heap space
OutOfMemoryError
Container killed due to OOM
```

#### 解决方案
```bash
# 1. 查看内存使用
free -h
docker stats

# 2. 调整JVM参数（在docker-compose.yml中）
environment:
  JAVA_OPTS: "-Xms128m -Xmx256m"

# 3. 重启服务
docker compose up -d backend
```

---

### 问题8: 端口被占用

#### 症状
```
Bind for 0.0.0.0:80 failed: port is already allocated
```

#### 解决方案
```bash
# 1. 查看端口占用
lsof -i:80
netstat -tlnp | grep 80

# 2. 停止占用进程
kill <PID>

# 3. 或修改端口映射（docker-compose.yml）
ports:
  - "8000:80"  # 改用8000端口
```

---

### 问题9: 镜像拉取失败

#### 症状
```
Error response from daemon: pull access denied
unable to access 'https://...': Could not resolve host
```

#### 解决方案
```bash
# 1. 检查网络
ping docker.io

# 2. 配置镜像加速器
cat > /etc/docker/daemon.json << EOF
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com",
    "https://registry.docker-cn.com"
  ]
}
EOF

# 3. 重启Docker
systemctl restart docker
```

---

### 问题10: Jenkins构建失败

#### 症状
```
mvn: not found
docker: command not found
```

#### 解决方案
```bash
# 1. 检查Jenkins容器挂载
docker inspect jenkins | grep -A 10 Mounts

# 2. 确保docker.sock已挂载
# docker-compose.jenkins.yml中应有:
volumes:
  - /var/run/docker.sock:/var/run/docker.sock
  - /usr/bin/docker:/usr/bin/docker

# 3. 使用Docker内的Maven
docker run --rm -v $(pwd):/app -w /app maven:3.9 mvn package
```

---

## 三、日志分析

### 3.1 查看日志命令
```bash
# 实时查看日志
docker logs -f ecommerce-backend

# 查看最近100行
docker logs --tail 100 ecommerce-backend

# 查看指定时间段
docker logs --since 2024-01-01 ecommerce-backend

# 导出日志
docker logs ecommerce-backend > backend.log 2>&1
```

### 3.2 常见错误日志

| 错误信息 | 含义 | 解决方案 |
|----------|------|----------|
| `Connection refused` | 连接被拒绝 | 检查目标服务是否启动 |
| `Connection timed out` | 连接超时 | 检查网络和防火墙 |
| `Table doesn't exist` | 表不存在 | 执行数据库初始化脚本 |
| `Access denied` | 权限不足 | 检查用户名密码 |
| `No space left` | 磁盘满 | 清理磁盘空间 |
| `OOM killed` | 内存不足 | 增加内存或优化配置 |

---

## 四、健康检查

### 4.1 服务健康检查
```bash
# 检查所有服务
docker compose ps

# 检查单个服务健康状态
docker inspect --format='{{.State.Health.Status}}' ecommerce-backend

# 手动健康检查
curl http://localhost:8080/actuator/health
curl http://localhost:80/health
```

### 4.2 自动化检查脚本
```bash
#!/bin/bash
# health-check.sh

echo "=== 服务健康检查 ==="

# 检查MySQL
if docker exec ecommerce-mysql mysqladmin ping -h localhost -u root -proot123456 &>/dev/null; then
    echo "✓ MySQL: 正常"
else
    echo "✗ MySQL: 异常"
fi

# 检查后端
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "✓ Backend: 正常"
else
    echo "✗ Backend: 异常"
fi

# 检查前端
if curl -s http://localhost:80/health | grep -q "OK"; then
    echo "✓ Frontend: 正常"
else
    echo "✗ Frontend: 异常"
fi
```

---

## 五、紧急恢复

### 5.1 完全重启
```bash
# 停止所有服务
docker compose down

# 清理（保留数据）
docker system prune -f

# 重新启动
docker compose up -d
```

### 5.2 数据恢复
```bash
# 从备份恢复数据库
docker exec -i ecommerce-mysql mysql -uroot -proot123456 ecommerce < backup.sql
```

### 5.3 回滚到上一版本
```bash
# 使用指定版本的镜像
docker compose down
docker tag ecommerce-backend:previous ecommerce-backend:latest
docker compose up -d
```

---

## 六、联系支持

如果以上方案无法解决问题，请收集以下信息：

1. 错误日志：`docker logs <container> > error.log 2>&1`
2. 系统信息：`docker info > system.log`
3. 容器状态：`docker ps -a > containers.log`
4. 网络信息：`docker network ls > network.log`

然后联系技术支持。
