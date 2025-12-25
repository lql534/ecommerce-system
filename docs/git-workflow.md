# Git 协作规范

## 一、分支管理策略

### 1.1 分支结构

```
main (主分支)
  │
  ├── develop (开发分支)
  │     │
  │     ├── feature/add-payment (功能分支)
  │     ├── feature/user-profile (功能分支)
  │     └── feature/order-export (功能分支)
  │
  ├── release/v1.0.0 (发布分支)
  │
  └── hotfix/fix-login-bug (热修复分支)
```

### 1.2 分支说明

| 分支类型 | 命名规范 | 说明 | 生命周期 |
|----------|----------|------|----------|
| main | main | 生产环境代码，始终保持可部署状态 | 永久 |
| develop | develop | 开发主分支，集成所有功能 | 永久 |
| feature | feature/功能名 | 新功能开发 | 临时 |
| release | release/版本号 | 发布准备 | 临时 |
| hotfix | hotfix/问题描述 | 紧急修复 | 临时 |

### 1.3 分支操作流程

#### 开发新功能
```bash
# 1. 从develop创建功能分支
git checkout develop
git pull origin develop
git checkout -b feature/add-cart-function

# 2. 开发并提交
git add .
git commit -m "feat: add shopping cart function"

# 3. 推送到远程
git push origin feature/add-cart-function

# 4. 创建Pull Request合并到develop
# 在GitHub上操作

# 5. 合并后删除功能分支
git branch -d feature/add-cart-function
git push origin --delete feature/add-cart-function
```

#### 发布版本
```bash
# 1. 从develop创建发布分支
git checkout develop
git checkout -b release/v1.0.0

# 2. 修复发布前的问题
git commit -m "fix: resolve release issues"

# 3. 合并到main并打标签
git checkout main
git merge release/v1.0.0
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin main --tags

# 4. 合并回develop
git checkout develop
git merge release/v1.0.0
git push origin develop

# 5. 删除发布分支
git branch -d release/v1.0.0
```

#### 紧急修复
```bash
# 1. 从main创建热修复分支
git checkout main
git checkout -b hotfix/fix-login-bug

# 2. 修复问题
git commit -m "fix: resolve login authentication issue"

# 3. 合并到main
git checkout main
git merge hotfix/fix-login-bug
git tag -a v1.0.1 -m "Hotfix: login bug"
git push origin main --tags

# 4. 合并到develop
git checkout develop
git merge hotfix/fix-login-bug
git push origin develop

# 5. 删除热修复分支
git branch -d hotfix/fix-login-bug
```

---

## 二、Commit 信息规范

### 2.1 Commit 格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 2.2 Type 类型

| 类型 | 说明 | 示例 |
|------|------|------|
| feat | 新功能 | `feat: add user login function` |
| fix | 修复Bug | `fix: resolve cart calculation error` |
| docs | 文档更新 | `docs: update API documentation` |
| style | 代码格式 | `style: format code with prettier` |
| refactor | 重构 | `refactor: optimize product service` |
| test | 测试 | `test: add unit tests for order service` |
| chore | 构建/工具 | `chore: update docker compose config` |
| perf | 性能优化 | `perf: optimize database queries` |
| ci | CI配置 | `ci: add GitHub Actions workflow` |

### 2.3 Scope 范围

| 范围 | 说明 |
|------|------|
| frontend | 前端相关 |
| backend | 后端相关 |
| database | 数据库相关 |
| docker | Docker配置 |
| k8s | Kubernetes配置 |
| ci | CI/CD配置 |
| docs | 文档 |

### 2.4 Commit 示例

```bash
# 好的commit信息
git commit -m "feat(backend): add order creation API"
git commit -m "fix(frontend): resolve cart item count display issue"
git commit -m "docs: update deployment guide"
git commit -m "refactor(backend): optimize product query performance"
git commit -m "test(backend): add unit tests for ProductService"
git commit -m "chore(docker): update nginx configuration"
git commit -m "ci: add GitHub Actions CI/CD workflow"

# 带详细说明的commit
git commit -m "feat(backend): add order status management

- Add order status enum (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED)
- Implement status transition validation
- Add status update API endpoints

Closes #123"
```

### 2.5 不好的commit示例

```bash
# ❌ 不好的commit信息
git commit -m "update"
git commit -m "fix bug"
git commit -m "修改了一些东西"
git commit -m "asdfasdf"
git commit -m "WIP"
```

---

## 三、Code Review 流程

### 3.1 Pull Request 流程

```
1. 开发者创建功能分支
         │
         ▼
2. 开发完成，推送到远程
         │
         ▼
3. 创建Pull Request
         │
         ▼
4. 自动化检查（CI）
         │
         ▼
5. Code Review（至少1人审核）
         │
         ▼
6. 修改反馈意见
         │
         ▼
7. 审核通过，合并代码
         │
         ▼
8. 删除功能分支
```

### 3.2 Pull Request 模板

```markdown
## 变更描述
<!-- 简要描述这个PR做了什么 -->

## 变更类型
- [ ] 新功能 (feat)
- [ ] Bug修复 (fix)
- [ ] 文档更新 (docs)
- [ ] 代码重构 (refactor)
- [ ] 测试 (test)
- [ ] 其他

## 测试说明
<!-- 描述如何测试这些变更 -->

## 检查清单
- [ ] 代码已自测通过
- [ ] 已添加必要的测试
- [ ] 文档已更新
- [ ] 代码符合规范

## 相关Issue
<!-- 关联的Issue编号，如 Closes #123 -->
```

### 3.3 Code Review 检查点

#### 代码质量
- [ ] 代码逻辑正确
- [ ] 无明显Bug
- [ ] 异常处理完善
- [ ] 无硬编码

#### 代码规范
- [ ] 命名规范
- [ ] 注释清晰
- [ ] 代码格式统一
- [ ] 无冗余代码

#### 安全性
- [ ] 无SQL注入风险
- [ ] 无敏感信息泄露
- [ ] 权限控制正确

#### 性能
- [ ] 无性能问题
- [ ] 数据库查询优化
- [ ] 无内存泄漏

### 3.4 Review 评论规范

```
# 必须修改
🔴 [MUST] 这里有SQL注入风险，需要使用参数化查询

# 建议修改
🟡 [SUGGEST] 建议将这个方法抽取为独立函数

# 疑问
🔵 [QUESTION] 这里为什么要这样处理？

# 赞赏
🟢 [NICE] 这个实现很优雅！
```

---

## 四、版本号规范

### 4.1 语义化版本

```
MAJOR.MINOR.PATCH
  │     │     │
  │     │     └── 补丁版本：Bug修复
  │     └──────── 次版本：新功能（向后兼容）
  └────────────── 主版本：重大变更（不兼容）
```

### 4.2 版本示例

| 版本 | 说明 |
|------|------|
| 1.0.0 | 首个正式版本 |
| 1.0.1 | Bug修复 |
| 1.1.0 | 新增功能 |
| 2.0.0 | 重大更新，不兼容旧版 |

### 4.3 打标签

```bash
# 创建标签
git tag -a v1.0.0 -m "Release version 1.0.0"

# 推送标签
git push origin v1.0.0

# 推送所有标签
git push origin --tags

# 查看标签
git tag -l

# 删除标签
git tag -d v1.0.0
git push origin --delete v1.0.0
```

---

## 五、Git 常用命令

### 5.1 基础命令
```bash
# 克隆仓库
git clone https://github.com/lql534/ecommerce-system.git

# 查看状态
git status

# 添加文件
git add .
git add <file>

# 提交
git commit -m "message"

# 推送
git push origin <branch>

# 拉取
git pull origin <branch>
```

### 5.2 分支命令
```bash
# 查看分支
git branch -a

# 创建分支
git checkout -b <branch>

# 切换分支
git checkout <branch>

# 删除分支
git branch -d <branch>

# 合并分支
git merge <branch>
```

### 5.3 撤销命令
```bash
# 撤销工作区修改
git checkout -- <file>

# 撤销暂存区
git reset HEAD <file>

# 撤销commit（保留修改）
git reset --soft HEAD^

# 撤销commit（丢弃修改）
git reset --hard HEAD^
```

### 5.4 查看历史
```bash
# 查看提交历史
git log --oneline

# 查看图形化历史
git log --graph --oneline --all

# 查看文件修改历史
git log -p <file>
```

---

## 六、.gitignore 配置

```gitignore
# IDE
.idea/
.vscode/
*.iml

# Build
target/
build/
dist/
node_modules/

# Logs
*.log
logs/

# Environment
.env.local
.env.*.local

# OS
.DS_Store
Thumbs.db

# Docker
docker-compose.override.yml

# Test
coverage/
*.test.js.snap
```
