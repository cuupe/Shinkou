# 02. 架构设计

## 1. 架构目标

Shinkou 的架构目标是支撑一个多工作区、多项目、可审计的需求影响分析平台。

核心目标：

```text
前后端分离
多工作区数据隔离
企业账号认证
项目文件索引
Agent 分析过程可追踪
AI 服务可替换
报告和任务可沉淀
安全认证和权限校验清晰
```

MVP 阶段优先保证业务链路跑通：

```text
登录 / 激活
→ 工作区
→ 项目
→ 文件
→ 需求分析
→ 任务草稿
→ 报告
```

## 2. 总体架构

```text
┌─────────────────────────────────────────────────────────────┐
│                         Browser                              │
│                                                             │
│  Vue / Vite / Pinia / Router / TanStack Query / Monaco       │
└───────────────────────────────┬─────────────────────────────┘
                                │ HTTPS / Cookie
                                ▼
┌─────────────────────────────────────────────────────────────┐
│                      Java Backend                            │
│                                                             │
│  Spring Boot / Spring Security / MyBatis-Plus                │
│                                                             │
│  Auth  Workspace  Project  File  Agent  Report  Task         │
└───────────────┬──────────────────────┬──────────────────────┘
                │                      │
                ▼                      ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│        PostgreSQL         │   │            Redis             │
│                           │   │                              │
│ users / workspaces        │   │ auth token / login lock      │
│ projects / files          │   │ short-lived state/cache      │
│ agent results / reports   │   │                              │
└──────────────────────────┘   └──────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────────────────┐
│                    Python AI Service                         │
│                                                             │
│  FastAPI / Pydantic / LangGraph reserved                     │
│  requirement analysis / tool orchestration / model adapters  │
└─────────────────────────────────────────────────────────────┘
```

当前实现说明：

```text
前端页面和 API 封装已具备较完整 MVP 形态。
后端已实现认证、工作区、项目等基础模块的一部分。
AI 服务目录存在，但 Docker Compose 默认未启用。
```

## 3. 模块划分

### 3.1 前端模块

目录：

```text
Shinkou-web/src
├─ api/          # HTTP API 封装
├─ components/   # 通用组件
├─ layouts/      # 工作区 / 项目布局
├─ pages/        # 页面
├─ router/       # 路由守卫
├─ services/     # 业务服务
├─ stores/       # Pinia 状态
├─ styles/       # 样式
├─ types/        # 类型定义
└─ utils/        # 工具函数
```

职责：

```text
登录和激活页面
工作区选择
项目工作台布局
项目管理
文件管理
代码探索
需求分析
任务看板
报告中心
模型配置
```

前端认证约定：

```text
不保存 accessToken 到 localStorage
所有请求携带 Cookie
通过 /api/auth/me 恢复登录态
退出登录调用后端 /api/auth/logout
```

### 3.2 Java 后端模块

目录概览：

```text
Shinkou-backend/src/main/java/com/cuupe/shinkou
├─ common/       # 响应、错误码、异常
├─ config/       # 安全、Redis、认证配置
├─ controller/   # HTTP Controller
├─ dto/          # 请求和响应 DTO
├─ eneity/       # 实体类
├─ mapper/       # MyBatis Mapper
├─ security/     # JWT、认证过滤器
├─ service/      # 业务接口
└─ util/         # 工具类
```

主要职责：

```text
认证与账号激活
工作区与成员权限
项目管理
文件上传与扫描
Agent 分析编排入口
报告与任务接口
统一错误响应
审计日志预留
```

### 3.3 Python AI Service

目录：

```text
Shinkou-ai
├─ app/
│  ├─ agents/
│  ├─ api/
│  ├─ clients/
│  ├─ core/
│  ├─ schemas/
│  └─ services/
├─ Dockerfile
└─ requirements.txt
```

目标职责：

```text
接收 Java 后端的内部分析请求
执行需求理解
调用文件搜索和读取工具
调用模型生成分析结果
返回 summary / affected_files / risks / tasks / steps
```

MVP 阶段可以先由 Java 后端模拟或同步调用，后续再接入完整 AI 服务。

## 4. 数据架构

核心数据表：

```text
users
workspaces
workspace_members
invitations
projects
project_files
agent_sessions
agent_tool_calls
analysis_affected_files
analysis_risks
analysis_task_drafts
reports
audit_logs
model_configs
tool_configs
```

核心关系：

```text
用户 N ─── N 工作区，通过 workspace_members 关联
工作区 1 ─── N 项目
项目 1 ─── N 文件
项目 1 ─── N Agent 分析会话
分析会话 1 ─── N 工具调用
分析会话 1 ─── N 影响文件 / 风险 / 任务草稿
```

多工作区隔离原则：

```text
所有项目级业务表必须保存 workspace_id
所有 workspace 路径接口必须校验当前用户是否为 ACTIVE 成员
所有 project 路径接口必须校验 project 属于当前 workspace
```

数据库详细设计见：

```text
docs/04-database.md
```

## 5. 认证架构

### 5.1 目标方案

推荐使用 HttpOnly Cookie 保存登录态：

```text
登录成功
→ 后端生成 JWT
→ Redis 保存 tokenId
→ 后端 Set-Cookie: access_token=<jwt>; HttpOnly
→ 浏览器自动携带 Cookie
→ 后端从 Cookie 解析 JWT
→ 校验 Redis 中 tokenId 是否有效
```

退出登录：

```text
浏览器请求 /api/auth/logout
→ 后端从 Cookie 解析 JWT
→ 删除 Redis auth:token:{userId}:{tokenId}
→ 返回 Max-Age=0 的过期 Cookie
→ 前端清理非敏感会话状态
```

### 5.2 当前实现差距

当前代码状态：

```text
前端已经启用 Cookie 请求方式。
后端当前 JwtAuthenticationFilter 主要从 Authorization: Bearer 读取 token。
后端 login / activate 仍返回 accessToken 到响应体。
后端 logout 仍从 Authorization Header 解析 token。
```

因此，要让 Cookie 方案完整生效，后端需要：

```text
login / activate 设置 HttpOnly Cookie
JwtAuthenticationFilter 支持从 Cookie 读取 access_token
logout 支持从 Cookie 读取 token 并清除 Cookie
CORS 开启 credentials
响应体不再暴露 accessToken
```

### 5.3 CSRF 策略

Cookie 认证需要考虑 CSRF。

MVP 建议：

```text
SameSite=Lax 或 Strict
所有写操作使用 POST / PATCH / DELETE
后端校验 Origin / Referer
生产环境使用 HTTPS + Secure Cookie
```

如果未来需要跨站点 Cookie，例如 `SameSite=None`，建议增加 CSRF Token。

## 6. 权限架构

权限校验分层：

```text
认证层：确认当前用户是谁
工作区层：确认用户是否属于 workspace
角色层：确认用户是否有操作权限
资源层：确认 project/file/session 属于该 workspace
```

工作区角色：

```text
OWNER > ADMIN > MEMBER
```

接口约束：

```text
/api/workspaces/{workspaceId}/... 必须校验 workspace 成员关系
/api/workspaces/{workspaceId}/projects/{projectId}/... 必须校验 project.workspaceId
管理员接口必须校验系统管理员或 OWNER/ADMIN 权限
```

## 7. 文件处理架构

MVP 文件链路：

```text
上传 ZIP
→ 保存临时文件
→ 校验文件类型和大小
→ 安全解压到项目目录
→ 过滤无关目录
→ 扫描文件元数据
→ 写入 project_files
→ 更新 projects.file_count
```

文件存储建议：

```text
data/workspaces/{workspaceId}/projects/{projectId}
```

安全要求：

```text
禁止路径穿越
禁止解压到项目根目录外
限制 ZIP 总大小和单文件大小
过滤 .git、node_modules、target、build、dist
读取文件必须使用相对路径并二次校验
```

## 8. Agent 分析架构

### 8.1 同步 MVP 流程

```text
前端提交需求
→ Java 后端创建 agent_session
→ Java 后端调用 AI 服务或本地模拟分析器
→ AI 服务调用工具读取项目上下文
→ 返回分析结果
→ Java 后端写入分析结果表
→ 前端展示结果
```

### 8.2 后续异步流程

```text
前端提交需求
→ Java 后端创建 PENDING session
→ 投递任务到队列或后台执行器
→ AI 服务异步分析
→ 分阶段写入 tool_calls
→ 前端轮询或 SSE/WebSocket 获取状态
→ 分析完成后展示结果
```

### 8.3 工具边界

Agent 工具建议由 Java 后端控制权限和路径：

```text
list_project_tree
search_text
read_file_lines
create_task_draft
generate_report
```

AI 服务不应直接任意读取服务器文件系统，应通过受控工具接口访问项目文件。

## 9. 前端架构

前端采用页面级路由和工作区布局：

```text
/login
/activate
/workspace-select
/workspaces/:workspaceId
  ├─ projects
  ├─ settings/models
  └─ projects/:projectId
      ├─ overview
      ├─ files
      ├─ code
      ├─ analysis
      ├─ tasks
      ├─ reports
      └─ runs
```

状态管理：

```text
auth.store       当前用户、工作区、登录态
workspace.store  工作区相关状态
project.store    当前项目状态
```

HTTP：

```text
api/http.ts 统一配置 baseURL、withCredentials、响应解包、401 处理
api/*.ts 按业务模块封装接口
```

布局原则：

```text
工作台页使用 WorkspaceLayout
项目内页使用 ProjectLayout
代码探索页允许占满视口高度
普通业务页使用最大宽度容器
```

## 10. 部署架构

Docker Compose 默认服务：

```text
frontend  Nginx 静态服务
backend   Spring Boot
postgres  PostgreSQL
redis     Redis
```

默认端口：

```text
frontend: http://localhost:3000
backend:  http://localhost:8080
postgres: localhost:5432
redis:    localhost:6379
```

生产建议：

```text
前端和后端统一挂在同站点域名下，减少跨域 Cookie 问题
使用 HTTPS
Cookie 开启 Secure
后端不要使用通配 CORS
数据库和 Redis 不暴露公网
上传文件目录单独挂载卷
```

## 11. 可观测性

建议加入：

```text
requestId
userId
workspaceId
projectId
sessionId
toolName
latencyMs
errorCode
```

审计日志记录：

```text
登录成功 / 失败
账号激活
退出登录
创建邀请
创建项目
上传 ZIP
发起分析
生成报告
删除项目
成员变更
```

## 12. 演进路线

### 阶段 1：MVP 闭环

```text
认证和工作区
项目管理
ZIP 上传和文件索引
关键词搜索和文件读取
需求影响分析
任务草稿和轨迹展示
```

### 阶段 2：真实 AI 编排

```text
Python AI Service 接入
模型配置生效
工具权限控制
异步分析
报告生成
```

### 阶段 3：企业化能力

```text
Git 仓库接入
PR Diff 分析
RAG 语义检索
审计日志
成员管理
SSO
项目级权限
```
