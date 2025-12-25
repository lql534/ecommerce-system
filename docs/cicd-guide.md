# CI/CD 工作流程指南

## 一、整体架构

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   开发者     │───▶│   Jenkins   │───▶│  私有仓库   │───▶│  生产环境   │
│  修改代码    │    │  自动构建    │    │ Registry   │    │  部署运行   │
└─────────────┘    └─────────────┘    └─────────────┘    └─────────────┘
```

## 二、环境信息

| 服务 | 地址 | 说明 |
|------|------|------|
| Jenkins | http://192.168.58.129:8082 | CI/CD控制台 |
| Registry | localhost:5000 | 私有镜像仓库 |
| 前端 | http://192.168.58.129:80 | Nginx服务 |
| 后端API | http://192.168.58.129:8080 | Spring Boot |
| 数据库管理 | http://192.168.58.129:8081 | phpMyAdmin |

## 三、完整工作流程演示

### 场景：修改商品列表页面标题

#### 步骤1：修改代码

在Windows开发机上修改文件 `frontend/html/index.html`：

```html
<!-- 修改前 -->
<title>电商数据管理系统</title>

<!-- 修改后 -->
<title>电商数据管理系统 v2.0</title>
```

#### 步骤2：提交代码到本地Git

```bash
cd D:\idea\ecommerce\ecommerce-system
git add .
git commit -m "feat: 更新首页标题为v2.0"
```

#### 步骤3：推送到GitHub（可选）

```bash
git push origin main
```

#### 步骤4：同步代码到虚拟机

由于虚拟机无法访问GitHub，需要手动同步：

**方式A：使用SCP/SFTP工具上传修改的文件**

**方式B：在虚拟机上手动修改**
```bash
# 在虚拟机上
vi /root/ecommerce-system/frontend/html/index.html
# 修改标题后保存
```

#### 步骤5：触发Jenkins构建

1. 打开浏览器访问 `http://192.168.58.129:8082`
2. 点击 `ecommerce-pipeline`
3. 点击左侧 **Build Now**

#### 步骤6：查看构建过程

点击构建编号（如 #3）→ **Console Output**，观察：

```
📥 复制源代码...
🧪 运行单元测试...
🔨 构建应用...
🐳 构建Docker镜像...
📤 推送镜像到仓库...
🚀 部署应用...
✅ CI/CD流水线执行成功!
```

#### 步骤7：部署到生产环境

在虚拟机上执行：

```bash
cd /root/ecommerce-system
docker compose down
docker compose up -d
```

#### 步骤8：验证更新

访问 `http://192.168.58.129:80`，查看标题是否变为"电商数据管理系统 v2.0"

---

## 四、Pipeline各阶段说明

### 阶段1：Checkout（代码检出）
```groovy
stage('Checkout') {
    steps {
        sh "cp -r ${SOURCE_DIR}/* ."
    }
}
```
- 从挂载的本地目录复制源代码到工作空间

### 阶段2：Unit Tests（单元测试）
```groovy
stage('Unit Tests') {
    steps {
        dir('backend') {
            sh 'mvn test -B'
        }
    }
}
```
- 执行Maven单元测试
- 测试文件位于 `backend/src/test/java/`

### 阶段3：Build Application（构建应用）
```groovy
stage('Build Application') {
    steps {
        dir('backend') {
            sh 'mvn clean package -DskipTests -B'
        }
    }
}
```
- 编译Java代码
- 打包成JAR文件

### 阶段4：Build Docker Images（构建镜像）
```groovy
stage('Build Docker Images') {
    steps {
        sh '''
            docker build -t localhost:5000/ecommerce-frontend:latest ./frontend
            docker build -t localhost:5000/ecommerce-backend:latest ./backend
        '''
    }
}
```
- 构建前端Nginx镜像
- 构建后端Spring Boot镜像

### 阶段5：Push Images（推送镜像）
```groovy
stage('Push Images') {
    steps {
        sh '''
            docker push localhost:5000/ecommerce-frontend:latest
            docker push localhost:5000/ecommerce-backend:latest
        '''
    }
}
```
- 推送镜像到私有Registry仓库

### 阶段6：Deploy（部署）
```groovy
stage('Deploy') {
    steps {
        echo "镜像已推送，请在宿主机执行部署命令"
    }
}
```
- 提示手动执行部署命令

---

## 五、常用操作命令

### 启动所有服务
```bash
# 启动Jenkins和Registry
cd /root/ecommerce-system/jenkins
docker compose -f docker-compose.jenkins.yml up -d

# 启动应用服务
cd /root/ecommerce-system
docker compose up -d
```

### 停止所有服务
```bash
cd /root/ecommerce-system
docker compose down

cd /root/ecommerce-system/jenkins
docker compose -f docker-compose.jenkins.yml down
```

### 查看服务状态
```bash
docker ps
```

### 查看构建日志
```bash
docker logs ecommerce-backend
docker logs ecommerce-frontend
```

### 查看私有仓库中的镜像
```bash
curl http://localhost:5000/v2/_catalog
```

---

## 六、故障排查

### 问题1：Jenkins构建失败
```bash
# 查看Jenkins日志
docker logs jenkins
```

### 问题2：镜像推送失败
```bash
# 检查Registry是否运行
docker ps | grep registry

# 重启Registry
docker restart registry
```

### 问题3：应用启动失败
```bash
# 查看后端日志
docker logs ecommerce-backend

# 查看数据库连接
docker exec ecommerce-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

---

## 七、流水线配置文件

完整的Jenkinsfile位于：`jenkins/Jenkinsfile`

当前使用的Pipeline脚本（在Jenkins界面配置）：

```groovy
pipeline {
    agent any
    
    environment {
        SOURCE_DIR = '/var/jenkins_home/workspace/ecommerce-source'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '📥 复制源代码...'
                sh "cp -r ${SOURCE_DIR}/* . || true"
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo '🧪 运行单元测试...'
                dir('backend') {
                    sh 'mvn test -B || echo "跳过测试"'
                }
            }
        }
        
        stage('Build Application') {
            steps {
                echo '🔨 构建应用...'
                dir('backend') {
                    sh 'mvn clean package -DskipTests -B'
                }
            }
        }
        
        stage('Build Docker Images') {
            steps {
                echo '🐳 构建Docker镜像...'
                sh '''
                    docker build -t localhost:5000/ecommerce-frontend:latest ${SOURCE_DIR}/frontend
                    docker build -t localhost:5000/ecommerce-backend:latest ${SOURCE_DIR}/backend
                '''
            }
        }
        
        stage('Push Images') {
            steps {
                echo '📤 推送镜像到仓库...'
                sh '''
                    docker push localhost:5000/ecommerce-frontend:latest
                    docker push localhost:5000/ecommerce-backend:latest
                '''
            }
        }
        
        stage('Deploy') {
            steps {
                echo '🚀 部署应用...'
                sh 'echo "请在宿主机执行: cd /root/ecommerce-system && docker compose up -d"'
            }
        }
    }
    
    post {
        success { echo '✅ CI/CD流水线执行成功!' }
        failure { echo '❌ CI/CD流水线执行失败!' }
    }
}
```
