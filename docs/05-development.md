# 05. 开发说明

## 1. 开发目标

本文档用于说明 Shinkou 的本地开发、调试、构建、认证联调和常见问题处理方式。

相关文档：

```text
docs/01-requirement.md   需求说明
docs/02-architecture.md  架构设计
docs/03-api.md           API 设计
docs/04-database.md      数据库设计
README.md                项目启动和总览
```

## 2. 环境要求

建议版本：

```text
Node.js 20+
npm 10+
Java 21
Maven 3.9+
Docker / Docker Compose
PostgreSQL 16
Redis 7
```

Windows 开发环境建议使用：

```text
PowerShell
Windows Terminal
Docker Desktop
IntelliJ IDEA / VS Code
```

## 3. 仓库结构

```text
Shinkou/
├─ Shinkou-web/       # Vue 前端
├─ Shinkou-backend/   # Spring Boot 后端
├─ Shinkou-ai/        # Python AI 服务预留
├─ docs/              # 项目文档
├─ sql/               # 数据库脚本
├─ docker-compose.yml
├─ .env.example
└─ README.md
```

## 4. 环境变量

从示例文件创建本地配置：

```bash
cp .env.example .env
```

关键变量：

```text
POSTGRES_DB=Shinkou
POSTGRES_USER=Shinkou
POSTGRES_PASSWORD=please_change_me
POSTGRES_PORT=5432

REDIS_PORT=6379
REDIS_DB=0
WEB_PORT=3000
APP_JWT_SECRET=please_change_me_to_at_least_32_chars

TZ=Asia/Shanghai
```

要求：

```text
APP_JWT_SECRET 至少 32 字节
生产环境禁止使用示例密码和默认 JWT secret
```

## 5. 本地启动

### 5.1 使用 Docker Compose 启动全部服务

```bash
docker-compose up --build
```

默认访问：

```text
前端：http://localhost:3000
后端：http://localhost:8080
PostgreSQL：localhost:5432
Redis：localhost:6379
```

### 5.2 只启动基础设施

如果需要本地运行前端和后端，只用 Docker 启动 PostgreSQL / Redis：

```bash
docker-compose up postgres redis
```

### 5.3 启动后端

```bash
cd Shinkou-backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

### 5.4 启动前端

```bash
cd Shinkou-web
npm install
npm run dev
```

前端默认地址：

```text
http://127.0.0.1:5173
```

前端 Vite 默认将 `/api` 代理到：

```text
http://127.0.0.1:8080
```

如果后端地址不同，可以设置：

```bash
VITE_API_PROXY_TARGET=http://127.0.0.1:8080 npm run dev
```

PowerShell：

```powershell
$env:VITE_API_PROXY_TARGET="http://127.0.0.1:8080"
npm run dev
```

## 6. 数据库初始化

数据库脚本：

```text
sql/shinkou_database_part1.sql
```

导入示例：

```bash
psql -h localhost -p 5432 -U Shinkou -d Shinkou -f sql/shinkou_database_part1.sql
```

如果使用 Docker 容器内的 PostgreSQL，可进入容器后导入，或使用本机 `psql` 连接映射端口。

初始化后，建议准备一条管理员邀请，例如：

```text
/activate?token=inv_demo_admin_token
```

具体初始化数据以 SQL 脚本为准。

## 7. 前端开发

### 7.1 目录说明

```text
Shinkou-web/src
├─ api/          # API 封装
├─ components/   # 通用组件
├─ layouts/      # 页面布局
├─ pages/        # 路由页面
├─ router/       # 路由配置和守卫
├─ services/     # 业务服务
├─ stores/       # Pinia 状态
├─ types/        # 类型定义
└─ utils/        # 工具函数
```

### 7.2 常用命令

```bash
npm install
npm run dev
npm run build
npm run preview
```

### 7.3 API 调用规范

统一使用：

```text
src/api/http.ts
```

约定：

```text
axios 开启 withCredentials: true
fetch 封装开启 credentials: include
前端不手动拼 Authorization: Bearer
前端不把 accessToken 存 localStorage
接口响应统一解包 Result.data
401 时清理前端非敏感会话状态并跳转登录
```

### 7.4 路由守卫

前端通过 `/api/auth/me` 恢复登录态：

```text
进入受保护路由
→ 如果未 hydrated，调用 /auth/me
→ 后端 Cookie 会话有效则返回用户和工作区
→ 前端继续校验 workspace 访问权限
→ 失败则跳转 /login
```

注意：

```text
当前前端已采用 Cookie 方案。
后端仍需支持 Set-Cookie 和从 Cookie 认证，否则 /auth/me 会失败。
```

### 7.5 页面开发约定

```text
普通工作台页面放在 pages/
工作区级布局使用 WorkspaceLayout
项目级嵌套路由使用 ProjectLayout
通用选择器、弹窗、空状态等放 components/
接口类型放 types/
业务 API 放 api/
```

响应式要求：

```text
固定多列布局需要在 2xl 或足够宽时启用
卡片和按钮文本需要截断或允许换行
表格横向滚动必须放在 table-wrap 中
长路径、文件名、报告名需要 break-all 或 truncate
```

## 8. 后端开发

### 8.1 目录说明

```text
Shinkou-backend/src/main/java/com/cuupe/shinkou
├─ common/       # 通用响应、错误码、异常
├─ config/       # 配置
├─ controller/   # Controller
├─ dto/          # DTO
├─ eneity/       # 实体
├─ mapper/       # MyBatis Mapper
├─ security/     # 认证和 JWT
├─ service/      # 业务服务
└─ util/         # 工具类
```

### 8.2 常用命令

```bash
mvn spring-boot:run
mvn test
mvn clean package
```

### 8.3 API 响应规范

统一返回：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {}
}
```

错误响应：

```json
{
  "code": "VALIDATION_ERROR",
  "message": "参数错误",
  "data": null
}
```

错误码见：

```text
docs/03-api.md
```

### 8.4 认证开发重点

目标方案：

```text
后端生成 JWT
Redis 保存 tokenId
后端通过 Set-Cookie 写入 HttpOnly Cookie
后端认证过滤器从 Cookie 读取 token
后端校验 Redis tokenId
退出登录删除 Redis token，并清除 Cookie
```

当前差距：

```text
JwtAuthenticationFilter 当前主要读取 Authorization Header。
AuthServiceImpl.logout 当前主要从 Authorization Header 解析 token。
login / activate 当前仍返回 accessToken 字段。
```

需要补齐：

```text
Cookie 工具类或常量
login / activate 写 Cookie
filter 从 Cookie 读取 access_token
logout 从 Cookie 获取 token 并清 Cookie
CORS 支持 credentials
响应体移除 accessToken 或设为兼容字段
```

### 8.5 权限开发重点

所有工作区接口必须校验：

```text
当前用户是否为 workspace 的 ACTIVE 成员
workspace 是否 ACTIVE
用户角色是否满足操作要求
```

所有项目接口必须校验：

```text
project 是否存在
project.workspaceId 是否等于路径 workspaceId
project 是否未被删除
```

### 8.6 文件开发重点

上传 ZIP 时必须：

```text
限制文件大小
限制文件类型
使用安全解压
校验解压目标路径
过滤无关目录
写入 project_files
更新 projects.file_count
```

读取文件时必须：

```text
只允许项目根目录下相对路径
禁止 ../ 路径穿越
限制返回内容大小
必要时按行读取
```

## 9. AI 服务开发

AI 服务目录当前为预留结构。

目标接口：

```http
POST /agent/analyze
```

输入：

```json
{
  "workspace_id": 1,
  "project_id": 1001,
  "requirement": "给订单系统增加优惠券抵扣功能"
}
```

输出：

```json
{
  "summary": "...",
  "affected_files": [],
  "risks": [],
  "tasks": [],
  "steps": []
}
```

AI 服务不应直接任意读服务器文件系统。推荐由 Java 后端暴露受控工具，AI 服务通过工具调用访问：

```text
list_project_tree
search_text
read_file_lines
create_task_draft
generate_report
```

## 10. 联调流程

### 10.1 认证联调

目标：

```text
登录成功后浏览器出现 access_token Cookie
Cookie 为 HttpOnly
前端无法通过 JS 读取 token
后续 /api/auth/me 自动携带 Cookie
退出登录后 Cookie 被清除
Redis token 被删除
```

检查点：

```text
浏览器 DevTools → Application → Cookies
Network → login 响应是否有 Set-Cookie
Network → me 请求是否带 Cookie
Network → logout 响应是否清 Cookie
Redis 中 auth:token:{userId}:{tokenId} 是否删除
```

### 10.2 项目联调

```text
登录
选择工作区
创建项目
刷新项目列表
进入项目概览
上传 ZIP
查看文件列表
搜索关键词
读取文件内容
```

### 10.3 Agent 联调

```text
准备有文件索引的项目
输入需求
调用 /agent/analyze
检查 agent_sessions
检查 agent_tool_calls
检查 affected_files / risks / task_drafts
前端展示结果
```

## 11. 测试建议

### 11.1 前端

目前主要依赖构建校验：

```bash
cd Shinkou-web
npm run build
```

建议后续补充：

```text
路由守卫测试
API 解包测试
认证状态恢复测试
关键表单交互测试
文件页面和分析页面组件测试
```

### 11.2 后端

```bash
cd Shinkou-backend
mvn test
```

建议覆盖：

```text
登录成功 / 失败
登录失败锁定
邀请激活
Cookie 签发和清除
JwtAuthenticationFilter
工作区权限校验
项目 code 重复
文件路径安全
```

## 12. 常见问题

### 12.1 Vite 代理 ECONNREFUSED

原因：

```text
前端代理目标没有服务监听
```

检查：

```text
后端是否启动在 8080
VITE_API_PROXY_TARGET 是否正确
Docker 端口是否映射
```

### 12.2 登录成功但刷新后回登录页

原因通常是：

```text
后端没有 Set-Cookie
Cookie 被浏览器拒绝
后端没有从 Cookie 读取 token
/api/auth/me 返回 401
```

### 12.3 Cookie 没有随请求发送

检查：

```text
前端 axios 是否 withCredentials=true
fetch 是否 credentials=include
后端 CORS 是否 allowCredentials=true
Access-Control-Allow-Origin 是否是明确 origin
SameSite / Secure 是否和当前环境匹配
```

### 12.4 Docker 后端启动失败

检查：

```text
APP_JWT_SECRET 是否设置且长度足够
PostgreSQL healthcheck 是否通过
Redis 是否启动
数据库账号密码是否和 .env 一致
```

### 12.5 前端构建 chunk 过大警告

Monaco Editor 会导致代码探索页 chunk 较大。当前属于警告，不影响构建结果。

后续可以优化：

```text
Monaco 单独懒加载
代码探索页拆分 chunk
配置 chunkSizeWarningLimit
```

## 13. Git 与协作约定

建议：

```text
不要提交 .env
不要提交真实密钥
不要提交数据库本地数据卷
后端和前端改动分别说明影响范围
接口变更同步更新 docs/03-api.md
表结构变更同步更新 docs/04-database.md 和 sql/
```

提交前至少执行：

```bash
cd Shinkou-web
npm run build
```

后端改动建议执行：

```bash
cd Shinkou-backend
mvn test
```

## 14. 开发优先级建议

近期优先级：

```text
1. 后端补齐 HttpOnly Cookie 认证链路
2. 工作区和项目接口稳定化
3. 项目文件上传、扫描、搜索、读取
4. Agent 分析会话数据落库
5. 报告和任务草稿接口完善
6. 管理员和成员管理
```
