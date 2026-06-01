# Shinkou

> A workspace-based engineering intelligence platform for requirement impact analysis, code exploration, task breakdown, and auditable delivery reports.

[![Status](https://img.shields.io/badge/status-MVP-blue)](#当前状态)
[![Frontend](https://img.shields.io/badge/frontend-Vue%20%2B%20Vite-42b883)](#技术栈)
[![Backend](https://img.shields.io/badge/backend-Spring%20Boot-6db33f)](#技术栈)
[![Java](https://img.shields.io/badge/Java-21-orange)](#环境要求)
[![License](https://img.shields.io/badge/license-MIT-green)](LICENSE)

Shinkou 是一个面向研发团队的需求变更影响分析平台。它帮助团队把“一个需求会影响什么”这件事结构化：从代码文件、接口、数据模型、技术风险，到任务拆解、测试建议和分析报告。

它不定位为 IDE 编码助手，也不直接替代 Cursor、Claude Code 或 GitHub Copilot。Shinkou 更适合需求评审、研发协作、变更风险评估和团队级研发知识沉淀。

## 快速链接

- [当前状态](#当前状态)
- [快速启动](#启动方式)
- [认证与安全](#认证与安全)
- [常用接口](#常用接口)
- [前端路由](#前端路由)
- [开发文档](docs/05-development.md)
- [API 设计](docs/03-api.md)
- [数据库设计](docs/04-database.md)

## 核心能力

- 创建工作区项目并管理代码资产
- 上传项目 ZIP 并建立文件索引
- 浏览文件树、搜索代码、读取文件内容
- 输入需求并生成影响分析结果
- 展示 Agent 工具调用轨迹
- 生成任务草稿、风险点和报告预览
- 配置模型、工具权限和分析参数

## 快速开始

```bash
cp .env.example .env
docker-compose up --build
```

默认访问地址：

- 前端：`http://localhost:3000`
- 后端：`http://localhost:8080`

本地前端开发：

```bash
cd Shinkou-web
npm install
npm run dev
```

## 当前状态

项目仍处于 MVP 开发阶段，仓库内包含：

- 前端工作台：登录、激活、工作区选择、项目管理、文件管理、代码探索、需求分析、任务看板、报告中心、模型配置等页面。
- Java 后端：认证、工作区、项目等基础接口正在实现。
- AI 服务目录：保留 FastAPI/LangGraph 服务结构，目前 Docker Compose 中默认未启用。
- 数据库脚本与设计文档：位于 `sql/` 和 `docs/`。

注意：前端已经按更安全的 Cookie 会话方式改造，所有请求会携带 Cookie；但当前后端认证代码仍主要是 `Authorization: Bearer <token>` 模式。要让认证链路完整工作，后端还需要补充 `HttpOnly Cookie` 的签发、读取和清除逻辑。

## 项目结构

```text
Shinkou/
├─ Shinkou-web/       # Vue + Vite 前端
├─ Shinkou-backend/   # Spring Boot 后端
├─ Shinkou-ai/        # AI 服务预留目录
├─ docs/              # 需求、架构、API、数据库、开发文档
├─ sql/               # 数据库脚本
├─ docker-compose.yml
├─ .env.example
└─ README.md
```

## 技术栈

Frontend:

- Vue
- TypeScript
- Vite
- Pinia
- Vue Router
- Tailwind CSS
- TanStack Query
- Monaco Editor
- Axios

Backend:

- Java 21
- Spring Boot
- Spring Security
- MyBatis-Plus
- PostgreSQL
- Redis / Redisson
- JWT

AI Service:

- FastAPI
- Pydantic
- LangGraph 预留

## MVP 功能范围

已覆盖或正在接入的页面与能力：

- 企业邮箱登录
- 邀请激活账号
- 工作区选择
- 项目创建与管理
- 项目 ZIP 上传入口
- 文件树与文件列表
- 代码文本搜索
- 代码探索工作台
- 需求影响分析
- Agent 执行轨迹
- 任务看板
- 报告中心
- 模型配置与工具配置界面

后续增强方向：

- Git 仓库接入
- 分支 / PR Diff 分析
- RAG 语义检索
- Reranker 精排
- 多智能体编排
- PDF 报告导出
- Human-in-the-loop 确认机制
- 权限与审计日志完善

## 环境要求

- Node.js 20+ 建议
- npm
- Java 21
- Maven
- Docker / Docker Compose
- PostgreSQL 16
- Redis 7

## 环境变量

复制示例文件：

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

`APP_JWT_SECRET` 必须至少 32 字节，用于 JWT 签名。不要在生产环境使用示例值或源码默认值。

## 启动方式

### Docker Compose

在仓库根目录执行：

```bash
docker-compose up --build
```

默认服务：

- 前端容器：`http://localhost:3000`
- 后端接口：`http://localhost:8080`
- PostgreSQL：`localhost:5432`
- Redis：`localhost:6379`

AI 服务目前在 `docker-compose.yml` 中是注释状态，等 `Shinkou-ai/app/main.py` 暴露完整 FastAPI app 后再启用。

### 本地前端开发

```bash
cd Shinkou-web
npm install
npm run dev
```

默认 Vite 地址：

```text
http://127.0.0.1:5173
```

前端 API 配置：

- 默认通过 Vite 代理访问 `/api`
- 代理目标默认是 `http://127.0.0.1:8080`
- 可通过 `VITE_API_PROXY_TARGET` 覆盖后端地址
- 如果设置 `VITE_API_BASE_URL`，前端会直接请求该地址

示例：

```bash
VITE_API_PROXY_TARGET=http://127.0.0.1:8080 npm run dev
```

Windows PowerShell 可使用：

```powershell
$env:VITE_API_PROXY_TARGET="http://127.0.0.1:8080"
npm run dev
```

### 本地后端开发

先启动 PostgreSQL 和 Redis，或使用 Docker Compose 只启动基础设施：

```bash
docker-compose up postgres redis
```

再启动后端：

```bash
cd Shinkou-backend
mvn spring-boot:run
```

后端默认端口：

```text
http://localhost:8080
```

## 数据库初始化

数据库脚本位于：

```text
sql/shinkou_database_part1.sql
```

如果本地数据库为空，需要先导入脚本。导入方式取决于你的 PostgreSQL 安装方式，例如：

```bash
psql -h localhost -p 5432 -U Shinkou -d Shinkou -f sql/shinkou_database_part1.sql
```

## 认证与安全

目标认证方案：

```text
后端登录成功
→ 后端 Set-Cookie 写入 HttpOnly 会话 Cookie
→ 前端请求自动携带 Cookie
→ 前端不接触 accessToken
→ 后端通过 Cookie 中的 JWT 或会话标识认证用户
→ 退出登录时后端删除 Redis 会话并清除 Cookie
```

前端当前已经完成的部分：

- Axios 开启 `withCredentials: true`
- Fetch 开启 `credentials: "include"`
- 不再把 `accessToken` 写入 `localStorage`
- 不再从 `localStorage` 读取 token 拼 `Authorization`
- 受保护路由通过 `/api/auth/me` 判断 Cookie 会话是否有效
- 退出登录调用 `POST /api/auth/logout`，成功后清理前端非敏感状态

后端当前仍需补齐的部分：

- 登录 `/api/auth/login` 成功后设置 `HttpOnly` Cookie
- 激活 `/api/auth/activate` 成功后设置 `HttpOnly` Cookie
- `JwtAuthenticationFilter` 从 Cookie 中读取 token
- `/api/auth/logout` 从 Cookie 中解析 tokenId，并返回过期 Cookie
- 跨域部署时配置 CORS `allowCredentials(true)`，且不能使用通配 `*` origin

建议 Cookie 属性：

```text
HttpOnly
Secure
SameSite=Lax 或 Strict
Path=/
Max-Age=<token ttl>
```

开发环境如果使用 HTTP，可以临时不启用 `Secure`；生产环境必须使用 HTTPS 并启用 `Secure`。

## 常用接口

认证：

- `POST /api/auth/login`
- `GET /api/auth/me`
- `POST /api/auth/logout`
- `GET /api/invitations/{token}`
- `POST /api/auth/activate`

工作区与项目：

- `GET /api/workspaces/my`
- `GET /api/workspaces/{workspaceId}`
- `GET /api/workspaces/{workspaceId}/projects`
- `POST /api/workspaces/{workspaceId}/projects`
- `GET /api/workspaces/{workspaceId}/projects/{projectId}`

文件与分析：

- `POST /api/workspaces/{workspaceId}/projects/{projectId}/upload-zip`
- `GET /api/workspaces/{workspaceId}/projects/{projectId}/files`
- `GET /api/workspaces/{workspaceId}/projects/{projectId}/files/tree`
- `GET /api/workspaces/{workspaceId}/projects/{projectId}/files/search`
- `POST /api/workspaces/{workspaceId}/projects/{projectId}/agent/analyze`

更完整的接口设计见：

```text
docs/03-api.md
```

## 前端路由

主要路由：

- `/login`
- `/activate?token=...`
- `/workspace-select`
- `/workspaces/:workspaceId/projects`
- `/workspaces/:workspaceId/projects/:projectId/overview`
- `/workspaces/:workspaceId/projects/:projectId/files`
- `/workspaces/:workspaceId/projects/:projectId/code`
- `/workspaces/:workspaceId/projects/:projectId/analysis`
- `/workspaces/:workspaceId/projects/:projectId/tasks`
- `/workspaces/:workspaceId/projects/:projectId/reports`
- `/workspaces/:workspaceId/projects/:projectId/runs`
- `/workspaces/:workspaceId/settings/models`

## 构建与验证

前端构建：

```bash
cd Shinkou-web
npm run build
```

后端测试：

```bash
cd Shinkou-backend
mvn test
```

当前前端构建可能出现 Monaco Editor 相关的大 chunk 提示，这是体积警告，不代表构建失败。

## 常见问题

### Vite 代理 `ECONNREFUSED`

说明前端代理目标没有服务在监听。检查：

- 后端是否启动在 `8080`
- `VITE_API_PROXY_TARGET` 是否配置正确
- Docker 容器端口是否映射

### 前端 Cookie 请求仍然未登录

检查后端是否已经：

- 返回 `Set-Cookie`
- Cookie 是否 `Path=/`
- 前后端跨域时 CORS 是否允许 credentials
- 浏览器是否因为 `SameSite` / `Secure` 拒绝 Cookie
- 后端认证过滤器是否从 Cookie 读取 token

### 登录成功但刷新后回到登录页

当前安全方案依赖 `/api/auth/me`。如果后端没有从 Cookie 识别用户，刷新后前端会判定会话无效。

## 文档

- `docs/01-requirement.md`：需求说明
- `docs/02-architecture.md`：架构设计
- `docs/03-api.md`：API 设计
- `docs/04-database.md`：数据库设计
- `docs/05-development.md`：开发说明

## License

见 `LICENSE`。
