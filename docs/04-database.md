# Database 表设计

## 设计原则

Shinkou 按企业内部研发平台设计，数据库需要支持：

```text
多工作区隔离
企业邮箱登录
邀请激活账号
工作区成员角色
项目归属工作区
后续扩展 Agent、RAG、报告、任务等模块
```

MVP 阶段优先实现：

```text
users
workspaces
workspace_members
invitations
projects
```

其中：

```text
users：用户是谁
workspaces：团队 / 工作区是什么
workspace_members：用户属于哪些工作区，以及在不同工作区的角色
invitations：邀请某个企业邮箱加入某个工作区
projects：某个工作区下的代码项目
```

---

## 表关系概览

```text
users
  1
  │
  │ user_id
  │
  N
workspace_members
  N
  │
  │ workspace_id
  │
  1
workspaces

workspaces 1 ─── N invitations
workspaces 1 ─── N projects
```

说明：

```text
一个用户可以加入多个工作区。
一个工作区可以有多个成员。
用户在不同工作区可以拥有不同角色。
项目必须归属于某一个工作区。
邀请记录用于控制内部账号激活，不开放普通注册。
```

---

# users 用户表

## 定义

用户表保存平台用户的基础信息和登录凭证。

注意：

```text
密码禁止明文保存，必须保存哈希值。
推荐使用 BCrypt。
```

## 表结构

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,

    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255),

    name VARCHAR(100) NOT NULL,
    avatar_url TEXT,

    department VARCHAR(100),
    position VARCHAR(100),

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    last_login_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段            | 类型           | 说明                  |
| --------------- | -------------- | --------------------- |
| `id`            | `BIGSERIAL`    | 用户 ID               |
| `email`         | `VARCHAR(255)` | 企业邮箱，全局唯一    |
| `password_hash` | `VARCHAR(255)` | 密码哈希，推荐 BCrypt |
| `name`          | `VARCHAR(100)` | 用户姓名              |
| `avatar_url`    | `TEXT`         | 头像地址，可选        |
| `department`    | `VARCHAR(100)` | 部门                  |
| `position`      | `VARCHAR(100)` | 职位                  |
| `status`        | `VARCHAR(50)`  | 用户状态              |
| `last_login_at` | `TIMESTAMP`    | 最近登录时间          |
| `created_at`    | `TIMESTAMP`    | 创建时间              |
| `updated_at`    | `TIMESTAMP`    | 更新时间              |

## status 枚举

| status     | 说明   |
| ---------- | ------ |
| `PENDING`  | 待激活 |
| `ACTIVE`   | 正常   |
| `DISABLED` | 已禁用 |
| `LOCKED`   | 已锁定 |

---

# workspaces 工作区表

## 定义

工作区表示一个团队、部门、公司空间或研发组织。

例如：

```text
Shinkou Engineering
AI Platform Team
QA Team
```

## 表结构

```sql
CREATE TABLE workspaces (
    id BIGSERIAL PRIMARY KEY,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段          | 类型           | 说明                                       |
| ------------- | -------------- | ------------------------------------------ |
| `id`          | `BIGSERIAL`    | 工作区 ID                                  |
| `name`        | `VARCHAR(255)` | 工作区名称，例如 `Shinkou Engineering`     |
| `code`        | `VARCHAR(100)` | 工作区唯一编码，例如 `Shinkou-engineering` |
| `description` | `TEXT`         | 工作区描述                                 |
| `status`      | `VARCHAR(50)`  | 工作区状态                                 |
| `created_by`  | `BIGINT`       | 创建者用户 ID，可以为空                    |
| `created_at`  | `TIMESTAMP`    | 创建时间                                   |
| `updated_at`  | `TIMESTAMP`    | 更新时间                                   |

## status 枚举

| status     | 说明   |
| ---------- | ------ |
| `ACTIVE`   | 正常   |
| `DISABLED` | 已禁用 |

---

# workspace_members 工作区成员表

## 定义

`workspace_members` 是用户与工作区之间的关系表。

它用于回答：

```text
用户属于哪个工作区？
用户在该工作区里是什么角色？
用户在该工作区里的成员状态是否有效？
```

角色不能直接放在 `users` 表，因为一个用户可以属于多个工作区，并且在不同工作区可以有不同角色。

示例：

| 用户 | 工作区              | 角色   |
| ---- | ------------------- | ------ |
| 张伟 | Shinkou Engineering | ADMIN  |
| 张伟 | AI Platform Team    | MEMBER |
| 李明 | Shinkou Engineering | MEMBER |

## 表结构

```sql
CREATE TABLE workspace_members (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    role VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_workspace_user UNIQUE (workspace_id, user_id)
);
```

## 字段说明

| 字段           | 类型          | 说明                     |
| -------------- | ------------- | ------------------------ |
| `id`           | `BIGSERIAL`   | 成员关系 ID              |
| `workspace_id` | `BIGINT`      | 工作区 ID                |
| `user_id`      | `BIGINT`      | 用户 ID                  |
| `role`         | `VARCHAR(50)` | 用户在当前工作区里的角色 |
| `status`       | `VARCHAR(50)` | 成员状态                 |
| `joined_at`    | `TIMESTAMP`   | 加入时间                 |
| `created_at`   | `TIMESTAMP`   | 创建时间                 |
| `updated_at`   | `TIMESTAMP`   | 更新时间                 |

## role 枚举

MVP 建议先支持：

| role     | 说明                             |
| -------- | -------------------------------- |
| `OWNER`  | 工作区所有者，最高权限           |
| `ADMIN`  | 管理员，可以邀请成员、管理项目   |
| `MEMBER` | 普通成员，可以使用项目和发起分析 |

后续可扩展：

| role      | 说明     |
| --------- | -------- |
| `VIEWER`  | 只读成员 |
| `AUDITOR` | 审计成员 |

## status 枚举

| status     | 说明   |
| ---------- | ------ |
| `ACTIVE`   | 正常   |
| `DISABLED` | 已禁用 |
| `REMOVED`  | 已移除 |

---

# invitations 邀请 / 账号激活表

## 定义

`invitations` 用于保存某个工作区对某个企业邮箱的邀请。

Shinkou 是企业内部平台，不开放普通注册。用户需要通过邀请链接激活账号。

邀请记录的含义是：

```text
邀请某个企业邮箱加入某个 workspace，并指定激活后的角色。
```

示例：

```text
邀请 liming@company.com 加入 Shinkou Engineering，角色 MEMBER
邀请 liming@company.com 加入 AI Platform Team，角色 ADMIN
```

这是两条不同邀请。

## 表结构

```sql
CREATE TABLE invitations (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,

    email VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    department VARCHAR(100),
    position VARCHAR(100),

    role VARCHAR(50) NOT NULL DEFAULT 'MEMBER',

    token VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    invited_by BIGINT,

    expires_at TIMESTAMP NOT NULL,
    accepted_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段           | 类型           | 说明                   |
| -------------- | -------------- | ---------------------- |
| `id`           | `BIGSERIAL`    | 邀请 ID                |
| `workspace_id` | `BIGINT`       | 邀请加入哪个工作区     |
| `email`        | `VARCHAR(255)` | 被邀请人的企业邮箱     |
| `name`         | `VARCHAR(100)` | 被邀请人姓名，可选     |
| `department`   | `VARCHAR(100)` | 部门，可选             |
| `position`     | `VARCHAR(100)` | 职位，可选             |
| `role`         | `VARCHAR(50)`  | 激活后在工作区中的角色 |
| `token`        | `VARCHAR(255)` | 邀请链接里的 token     |
| `status`       | `VARCHAR(50)`  | 邀请状态               |
| `invited_by`   | `BIGINT`       | 邀请人用户 ID          |
| `expires_at`   | `TIMESTAMP`    | 过期时间               |
| `accepted_at`  | `TIMESTAMP`    | 接受邀请时间           |
| `created_at`   | `TIMESTAMP`    | 创建时间               |
| `updated_at`   | `TIMESTAMP`    | 更新时间               |

## status 枚举

| status     | 说明   |
| ---------- | ------ |
| `PENDING`  | 待接受 |
| `ACCEPTED` | 已接受 |
| `EXPIRED`  | 已过期 |
| `REVOKED`  | 已撤销 |

---

# projects 项目表

## 定义

`projects` 用于保存某个工作区下的代码项目。

多工作区下，项目必须归属于某个 `workspace`。

例如：

```text
Shinkou Engineering
  - Order System Demo
  - Mall Backend
  - User Center

AI Platform Team
  - RAG Platform
  - Agent Eval Service
```

## 表结构

推荐保留 `code` 字段，便于前端路由、文件存储路径和后续 Git 仓库标识。

```sql
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,

    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    description TEXT,

    root_path TEXT,
    file_count INTEGER NOT NULL DEFAULT 0,

    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',

    created_by BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_project_workspace_code UNIQUE (workspace_id, code)
);
```

## 字段说明

| 字段           | 类型           | 说明                           |
| -------------- | -------------- | ------------------------------ |
| `id`           | `BIGSERIAL`    | 项目 ID                        |
| `workspace_id` | `BIGINT`       | 所属工作区 ID                  |
| `name`         | `VARCHAR(255)` | 项目名称                       |
| `code`         | `VARCHAR(100)` | 项目唯一编码，同一工作区内唯一 |
| `description`  | `TEXT`         | 项目描述                       |
| `root_path`    | `TEXT`         | 项目文件在服务器上的存储路径   |
| `file_count`   | `INTEGER`      | 项目文件数量                   |
| `status`       | `VARCHAR(50)`  | 项目状态                       |
| `created_by`   | `BIGINT`       | 创建人用户 ID                  |
| `created_at`   | `TIMESTAMP`    | 创建时间                       |
| `updated_at`   | `TIMESTAMP`    | 更新时间                       |

## status 枚举

| status     | 说明                 |
| ---------- | -------------------- |
| `ACTIVE`   | 正常项目             |
| `ARCHIVED` | 已归档，不再主动分析 |
| `DELETED`  | 软删除               |

## 文件存储路径建议

项目文件建议按 `workspaceId + projectId` 存储：

```text
data/workspaces/{workspaceId}/projects/{projectId}
```

例如：

```text
data/workspaces/1/projects/1001
```

这样即使项目 `code` 改名，文件路径也不受影响。

---

# 外键约束

```sql
ALTER TABLE workspaces
ADD CONSTRAINT fk_workspaces_created_by
FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE workspace_members
ADD CONSTRAINT fk_workspace_members_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE workspace_members
ADD CONSTRAINT fk_workspace_members_user
FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE invitations
ADD CONSTRAINT fk_invitations_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE invitations
ADD CONSTRAINT fk_invitations_invited_by
FOREIGN KEY (invited_by) REFERENCES users(id);

ALTER TABLE projects
ADD CONSTRAINT fk_projects_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE projects
ADD CONSTRAINT fk_projects_created_by
FOREIGN KEY (created_by) REFERENCES users(id);
```

---

# 索引设计

```sql
CREATE INDEX idx_users_email
ON users(email);

CREATE INDEX idx_users_status
ON users(status);

CREATE INDEX idx_workspaces_code
ON workspaces(code);

CREATE INDEX idx_workspaces_status
ON workspaces(status);

CREATE INDEX idx_workspace_members_workspace_id
ON workspace_members(workspace_id);

CREATE INDEX idx_workspace_members_user_id
ON workspace_members(user_id);

CREATE INDEX idx_workspace_members_role
ON workspace_members(role);

CREATE INDEX idx_invitations_token
ON invitations(token);

CREATE INDEX idx_invitations_email
ON invitations(email);

CREATE INDEX idx_invitations_workspace_id
ON invitations(workspace_id);

CREATE INDEX idx_invitations_status
ON invitations(status);

CREATE UNIQUE INDEX uk_pending_invitation_workspace_email
ON invitations(workspace_id, email)
WHERE status = 'PENDING';

CREATE INDEX idx_projects_workspace_id
ON projects(workspace_id);

CREATE INDEX idx_projects_created_by
ON projects(created_by);

CREATE INDEX idx_projects_status
ON projects(status);

CREATE INDEX idx_projects_workspace_status
ON projects(workspace_id, status);
```

---

# 初始化数据示例

MVP 阶段可以先不做管理端，直接通过 SQL 初始化一个工作区和一条管理员邀请。

```sql
INSERT INTO workspaces (
    name,
    code,
    description,
    status
)
VALUES (
    'Shinkou Engineering',
    'Shinkou-engineering',
    'Shinkou 示例研发团队',
    'ACTIVE'
);

INSERT INTO invitations (
    workspace_id,
    email,
    name,
    department,
    position,
    role,
    token,
    status,
    expires_at
)
VALUES (
    1,
    'admin@company.com',
    '系统管理员',
    '研发平台部',
    '平台管理员',
    'ADMIN',
    'inv_demo_admin_token',
    'PENDING',
    NOW() + INTERVAL '30 days'
);
```

然后前端访问：

```text
/activate?token=inv_demo_admin_token
```

激活账号后，后端应：

```text
1. 创建 users 记录，status = ACTIVE
2. 创建 workspace_members 记录
3. 更新 invitations.status = ACCEPTED
4. 写入 invitations.accepted_at
```

---

## audit_logs 审计日志表

### 定义

审计日志记录的是：

```
谁
在什么时间
对哪个资源
做了什么操作
结果是成功还是失败
来源 IP / User-Agent 是什么
```

比如：

```
用户登录成功
用户登录失败
用户激活账号
管理员创建邀请
用户创建项目
用户上传 ZIP
用户发起 AI 影响分析
用户删除项目
```

这些是需要长期追踪的。

### 表结构

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT,
    user_id BIGINT,
    
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100),
    resource_id BIGINT,
    
    result VARCHAR(50) NOT NULL,
    
    ip_address VARCHAR(100),
    user_agent TEXT,
    
    request_id VARCHAR(100),
    
    message TEXT,
    detail JSONB,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

);
```

### 字段说明

| 字段            | 说明                                             |
| --------------- | ------------------------------------------------ |
| `id`            | 日志 ID                                          |
| `workspace_id`  | 所属工作区，可为空                               |
| `user_id`       | 操作用户，可为空，比如登录失败时可能查不到用户   |
| `action`        | 操作类型                                         |
| `resource_type` | 资源类型，比如 `USER` / `PROJECT` / `INVITATION` |
| `resource_id`   | 资源 ID                                          |
| `result`        | 操作结果，比如 `SUCCESS` / `FAILURE`             |
| `ip_address`    | 请求 IP                                          |
| `user_agent`    | 浏览器 / 客户端信息                              |
| `request_id`    | 请求链路 ID，方便排查问题                        |
| `message`       | 简短说明                                         |
| `detail`        | JSON 详情                                        |
| `created_at`    | 创建时间                                         |

### action枚举

### 认证相关

| 枚举                    | 含义             |
| ----------------------- | ---------------- |
| `AUTH_LOGIN_SUCCESS`    | 用户登录成功     |
| `AUTH_LOGIN_FAILED`     | 用户登录失败     |
| `AUTH_LOGOUT`           | 用户退出登录     |
| `AUTH_ACTIVATE_SUCCESS` | 用户激活账号成功 |
| `AUTH_ACTIVATE_FAILED`  | 用户激活账号失败 |

### 邀请相关

| 枚举                | 含义               |
| ------------------- | ------------------ |
| `INVITATION_CREATE` | 创建邀请           |
| `INVITATION_REVOKE` | 撤销邀请           |
| `INVITATION_ACCEPT` | 接受邀请并激活账号 |

### 工作区成员相关

| 枚举                           | 含义             |
| ------------------------------ | ---------------- |
| `WORKSPACE_MEMBER_ADD`         | 添加成员到工作区 |
| `WORKSPACE_MEMBER_REMOVE`      | 移除工作区成员   |
| `WORKSPACE_MEMBER_ROLE_CHANGE` | 修改成员角色     |

### 项目相关

| 枚举                 | 含义                |
| -------------------- | ------------------- |
| `PROJECT_CREATE`     | 创建项目            |
| `PROJECT_UPDATE`     | 修改项目            |
| `PROJECT_DELETE`     | 删除或软删除项目    |
| `PROJECT_UPLOAD_ZIP` | 上传项目 ZIP 代码包 |

### AI 分析相关

| 枚举                    | 含义             |
| ----------------------- | ---------------- |
| `AGENT_ANALYZE_START`   | 用户发起 AI 分析 |
| `AGENT_ANALYZE_SUCCESS` | AI 分析成功完成  |
| `AGENT_ANALYZE_FAILED`  | AI 分析失败      |

# project_files 项目文件表

## 定义

`project_files` 用于保存某个项目下扫描出来的文件索引。

项目上传 ZIP 并解压后，后端会扫描文件并写入该表。

AI 分析、文件树展示、代码搜索、读取文件片段都依赖该表。

它用于回答：

```
某个项目有哪些文件？
文件路径是什么？
文件语言是什么？
文件是否已经被索引？
文件大小是多少？
```

注意：

```
project_files 必须同时包含 workspace_id 和 project_id。
```

这样方便多工作区权限过滤与查询优化。

## 表结构

```sql
CREATE TABLE project_files (
    id BIGSERIAL PRIMARY KEY,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    file_path TEXT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_ext VARCHAR(50),
    language VARCHAR(50),

    size BIGINT,
    checksum VARCHAR(128),

    indexed BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_project_file_path UNIQUE (project_id, file_path)
);
```

## 字段说明

| 字段           | 类型           | 说明                                  |
| -------------- | -------------- | ------------------------------------- |
| `id`           | `BIGSERIAL`    | 文件 ID                               |
| `workspace_id` | `BIGINT`       | 所属工作区 ID                         |
| `project_id`   | `BIGINT`       | 所属项目 ID                           |
| `file_path`    | `TEXT`         | 文件相对路径                          |
| `file_name`    | `VARCHAR(255)` | 文件名                                |
| `file_ext`     | `VARCHAR(50)`  | 文件扩展名，例如 `.java` / `.vue`     |
| `language`     | `VARCHAR(50)`  | 文件语言，例如 `Java` / `Vue` / `SQL` |
| `size`         | `BIGINT`       | 文件大小，单位字节                    |
| `checksum`     | `VARCHAR(128)` | 文件校验值，用于判断文件是否变化      |
| `indexed`      | `BOOLEAN`      | 是否已建立索引                        |
| `created_at`   | `TIMESTAMP`    | 创建时间                              |
| `updated_at`   | `TIMESTAMP`    | 更新时间                              |

------

# agent_sessions Agent 分析会话表

## 定义

`agent_sessions` 用于保存一次 AI 需求影响分析会话。

用户在项目中输入一段需求后，系统会创建一条分析会话记录。

例如：

```
给订单系统增加优惠券抵扣功能，用户下单时可以选择一张优惠券，订单金额需要重新计算。
```

该表记录：

```
谁发起的分析
在哪个工作区
分析哪个项目
需求内容是什么
当前分析状态是什么
最终总结是什么
失败原因是什么
```

## 表结构

```sql
CREATE TABLE agent_sessions (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL UNIQUE,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,

    requirement TEXT NOT NULL,

    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',

    summary TEXT,
    error_message TEXT,

    started_at TIMESTAMP,
    completed_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段            | 类型           | 说明                                     |
| --------------- | -------------- | ---------------------------------------- |
| `id`            | `BIGSERIAL`    | 分析会话 ID                              |
| `session_id`    | `VARCHAR(100)` | 业务会话 ID，例如 `as_20260518100000001` |
| `workspace_id`  | `BIGINT`       | 所属工作区 ID                            |
| `project_id`    | `BIGINT`       | 所属项目 ID                              |
| `user_id`       | `BIGINT`       | 发起分析的用户 ID                        |
| `requirement`   | `TEXT`         | 用户输入的需求内容                       |
| `status`        | `VARCHAR(50)`  | 分析状态                                 |
| `summary`       | `TEXT`         | AI 生成的分析总结                        |
| `error_message` | `TEXT`         | 分析失败原因                             |
| `started_at`    | `TIMESTAMP`    | 分析开始时间                             |
| `completed_at`  | `TIMESTAMP`    | 分析完成时间                             |
| `created_at`    | `TIMESTAMP`    | 创建时间                                 |
| `updated_at`    | `TIMESTAMP`    | 更新时间                                 |

## status 枚举

| status      | 说明     |
| ----------- | -------- |
| `PENDING`   | 等待分析 |
| `RUNNING`   | 分析中   |
| `COMPLETED` | 分析完成 |
| `FAILED`    | 分析失败 |
| `CANCELLED` | 已取消   |

------

# agent_tool_calls Agent 工具调用表

## 定义

`agent_tool_calls` 用于保存 Agent 在一次分析过程中调用工具的轨迹。

它不是审计日志，而是 AI 分析过程记录。

例如一次需求分析中，Agent 可能会执行：

```
搜索 coupon 关键字
读取 OrderService.java 第 1-120 行
搜索 createOrder 方法
读取 PaymentService.java 相关代码片段
生成任务草稿
```

这些工具调用步骤都可以写入该表。

该表用于支持：

```
前端展示 Agent 工具调用轨迹
排查 AI 为什么得出某个结论
分析 Agent 搜索和读取文件的过程
后续优化 Prompt 和工具策略
```

## 表结构

```sql
CREATE TABLE agent_tool_calls (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    tool_name VARCHAR(100) NOT NULL,

    arguments JSONB,
    result JSONB,

    status VARCHAR(50) NOT NULL DEFAULT 'SUCCESS',

    latency_ms INTEGER,

    error_message TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段            | 类型           | 说明                   |
| --------------- | -------------- | ---------------------- |
| `id`            | `BIGSERIAL`    | 工具调用 ID            |
| `session_id`    | `VARCHAR(100)` | 所属分析会话 ID        |
| `workspace_id`  | `BIGINT`       | 所属工作区 ID          |
| `project_id`    | `BIGINT`       | 所属项目 ID            |
| `tool_name`     | `VARCHAR(100)` | 工具名称               |
| `arguments`     | `JSONB`        | 工具调用参数           |
| `result`        | `JSONB`        | 工具调用结果           |
| `status`        | `VARCHAR(50)`  | 调用状态               |
| `latency_ms`    | `INTEGER`      | 工具调用耗时，单位毫秒 |
| `error_message` | `TEXT`         | 调用失败原因           |
| `created_at`    | `TIMESTAMP`    | 创建时间               |

## tool_name 示例

| tool_name             | 说明             |
| --------------------- | ---------------- |
| `search_text`         | 搜索文件内容     |
| `read_file`           | 读取完整文件     |
| `read_lines`          | 读取文件指定行   |
| `list_files`          | 获取项目文件列表 |
| `analyze_requirement` | 分析需求         |
| `generate_tasks`      | 生成任务草稿     |

## status 枚举

| status    | 说明     |
| --------- | -------- |
| `SUCCESS` | 调用成功 |
| `FAILED`  | 调用失败 |
| `SKIPPED` | 已跳过   |

注意：

```
result 字段不建议长期保存大段源码。
可以保存命中文件、命中数量、行号、摘要等信息。
完整代码片段建议按需读取，避免数据库膨胀。
```

------

# analysis_affected_files 影响文件表

## 定义

`analysis_affected_files` 用于保存 AI 分析后判断可能受影响的文件。

例如需求是：

```
给订单系统增加优惠券抵扣功能
```

AI 可能判断这些文件受影响：

```
OrderService.java
CouponService.java
PaymentService.java
OrderController.java
```

该表保存每个受影响文件的路径、影响行号、影响原因和置信度。

## 表结构

```sql
CREATE TABLE analysis_affected_files (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    file_path TEXT NOT NULL,
    file_name VARCHAR(255),
    language VARCHAR(50),

    line_start INTEGER,
    line_end INTEGER,

    reason TEXT NOT NULL,

    confidence NUMERIC(5, 4),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段           | 类型            | 说明                   |
| -------------- | --------------- | ---------------------- |
| `id`           | `BIGSERIAL`     | 影响文件记录 ID        |
| `session_id`   | `VARCHAR(100)`  | 所属分析会话 ID        |
| `workspace_id` | `BIGINT`        | 所属工作区 ID          |
| `project_id`   | `BIGINT`        | 所属项目 ID            |
| `file_path`    | `TEXT`          | 受影响文件路径         |
| `file_name`    | `VARCHAR(255)`  | 文件名                 |
| `language`     | `VARCHAR(50)`   | 文件语言               |
| `line_start`   | `INTEGER`       | 起始行                 |
| `line_end`     | `INTEGER`       | 结束行                 |
| `reason`       | `TEXT`          | 判断该文件受影响的原因 |
| `confidence`   | `NUMERIC(5, 4)` | 置信度，例如 `0.9200`  |
| `created_at`   | `TIMESTAMP`     | 创建时间               |

------

# analysis_risks 风险点表

## 定义

`analysis_risks` 用于保存 AI 在需求影响分析中识别出的风险点。

例如：

```
订单金额和支付金额可能不一致
优惠券过期、不可用、门槛不足需要处理
已有订单流程可能受到影响
```

该表用于前端展示风险列表，也可以用于后续生成分析报告。

## 表结构

```sql
CREATE TABLE analysis_risks (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    risk_level VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',

    description TEXT NOT NULL,

    suggestion TEXT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段           | 类型           | 说明            |
| -------------- | -------------- | --------------- |
| `id`           | `BIGSERIAL`    | 风险 ID         |
| `session_id`   | `VARCHAR(100)` | 所属分析会话 ID |
| `workspace_id` | `BIGINT`       | 所属工作区 ID   |
| `project_id`   | `BIGINT`       | 所属项目 ID     |
| `risk_level`   | `VARCHAR(50)`  | 风险等级        |
| `description`  | `TEXT`         | 风险说明        |
| `suggestion`   | `TEXT`         | 处理建议        |
| `created_at`   | `TIMESTAMP`    | 创建时间        |

## risk_level 枚举

| risk_level | 说明     |
| ---------- | -------- |
| `LOW`      | 低风险   |
| `MEDIUM`   | 中风险   |
| `HIGH`     | 高风险   |
| `CRITICAL` | 严重风险 |

------

# analysis_task_drafts 任务草稿表

## 定义

`analysis_task_drafts` 用于保存 AI 根据需求影响分析结果拆分出的任务草稿。

例如：

```
修改订单创建流程中的优惠券抵扣逻辑
增加优惠券有效性校验
补充订单金额计算测试
```

这些任务最初只是 AI 生成的草稿，用户可以接受、拒绝，或者后续转成正式任务。

## 表结构

```sql
CREATE TABLE analysis_task_drafts (
    id BIGSERIAL PRIMARY KEY,

    session_id VARCHAR(100) NOT NULL,

    workspace_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,

    title VARCHAR(255) NOT NULL,

    priority VARCHAR(50) NOT NULL DEFAULT 'P2',

    description TEXT,

    related_files JSONB,

    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## 字段说明

| 字段            | 类型           | 说明            |
| --------------- | -------------- | --------------- |
| `id`            | `BIGSERIAL`    | 任务草稿 ID     |
| `session_id`    | `VARCHAR(100)` | 所属分析会话 ID |
| `workspace_id`  | `BIGINT`       | 所属工作区 ID   |
| `project_id`    | `BIGINT`       | 所属项目 ID     |
| `title`         | `VARCHAR(255)` | 任务标题        |
| `priority`      | `VARCHAR(50)`  | 任务优先级      |
| `description`   | `TEXT`         | 任务描述        |
| `related_files` | `JSONB`        | 相关文件列表    |
| `status`        | `VARCHAR(50)`  | 任务草稿状态    |
| `created_at`    | `TIMESTAMP`    | 创建时间        |
| `updated_at`    | `TIMESTAMP`    | 更新时间        |

## priority 枚举

| priority | 说明       |
| -------- | ---------- |
| `P0`     | 最高优先级 |
| `P1`     | 高优先级   |
| `P2`     | 普通优先级 |
| `P3`     | 低优先级   |

## status 枚举

| status      | 说明           |
| ----------- | -------------- |
| `DRAFT`     | 草稿           |
| `ACCEPTED`  | 已接受         |
| `REJECTED`  | 已拒绝         |
| `CONVERTED` | 已转为正式任务 |

------

# AI 相关表关系概览

```
projects
  1
  │
  │ project_id
  │
  N
project_files

projects
  1
  │
  │ project_id
  │
  N
agent_sessions

agent_sessions
  1
  ├── N agent_tool_calls
  ├── N analysis_affected_files
  ├── N analysis_risks
  └── N analysis_task_drafts
```

说明：

```
一个项目可以有多个文件。
一个项目可以发起多次 AI 分析。
一次 AI 分析会话可以包含多条工具调用记录。
一次 AI 分析会话可以生成多个受影响文件、多个风险点和多个任务草稿。
```

------

# AI 相关外键约束

```sql
ALTER TABLE project_files
ADD CONSTRAINT fk_project_files_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE project_files
ADD CONSTRAINT fk_project_files_project
FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE agent_sessions
ADD CONSTRAINT fk_agent_sessions_workspace
FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

ALTER TABLE agent_sessions
ADD CONSTRAINT fk_agent_sessions_project
FOREIGN KEY (project_id) REFERENCES projects(id);

ALTER TABLE agent_sessions
ADD CONSTRAINT fk_agent_sessions_user
FOREIGN KEY (user_id) REFERENCES users(id);
```

说明：

```
agent_tool_calls、analysis_affected_files、analysis_risks、analysis_task_drafts 可以先不加外键。
它们通过 session_id 与 agent_sessions 关联。
这样后续清理历史分析数据时更灵活。
```

------

# AI 相关索引设计

```sql
CREATE INDEX idx_project_files_workspace_id
ON project_files(workspace_id);

CREATE INDEX idx_project_files_project_id
ON project_files(project_id);

CREATE INDEX idx_project_files_language
ON project_files(language);

CREATE INDEX idx_project_files_indexed
ON project_files(indexed);

CREATE INDEX idx_agent_sessions_workspace_id
ON agent_sessions(workspace_id);

CREATE INDEX idx_agent_sessions_project_id
ON agent_sessions(project_id);

CREATE INDEX idx_agent_sessions_user_id
ON agent_sessions(user_id);

CREATE INDEX idx_agent_sessions_status
ON agent_sessions(status);

CREATE INDEX idx_agent_sessions_created_at
ON agent_sessions(created_at);

CREATE INDEX idx_agent_tool_calls_session_id
ON agent_tool_calls(session_id);

CREATE INDEX idx_agent_tool_calls_project_id
ON agent_tool_calls(project_id);

CREATE INDEX idx_agent_tool_calls_tool_name
ON agent_tool_calls(tool_name);

CREATE INDEX idx_analysis_affected_files_session_id
ON analysis_affected_files(session_id);

CREATE INDEX idx_analysis_affected_files_project_id
ON analysis_affected_files(project_id);

CREATE INDEX idx_analysis_risks_session_id
ON analysis_risks(session_id);

CREATE INDEX idx_analysis_risks_project_id
ON analysis_risks(project_id);

CREATE INDEX idx_analysis_task_drafts_session_id
ON analysis_task_drafts(session_id);

CREATE INDEX idx_analysis_task_drafts_project_id
ON analysis_task_drafts(project_id);

CREATE INDEX idx_analysis_task_drafts_status
ON analysis_task_drafts(status);
```

------

# 后续扩展表

AI 模块后续可以继续增加：

```
code_chunks
embedding_jobs
model_configs
tool_configs
reports
```

其中：

```
code_chunks：保存代码切片，用于 RAG 检索
embedding_jobs：保存向量化任务
model_configs：保存模型配置
tool_configs：保存 Agent 工具配置
reports：保存分析报告
```

MVP 阶段建议优先实现：

```
project_files
agent_sessions
agent_tool_calls
analysis_affected_files
analysis_risks
analysis_task_drafts
```

这样就可以覆盖：

```
项目文件扫描
AI 分析会话
Agent 工具调用轨迹
影响文件展示
风险展示
任务草稿展示
```

# 后续扩展表

MVP 之后可继续增加：

```text
project_files
agent_sessions
agent_tool_calls
analysis_results
task_drafts
reports
code_chunks
embedding_jobs
model_configs
tool_configs
audit_logs
```

其中 `project_files` 应同时包含：

```text
workspace_id
project_id
```

方便后续做多工作区权限过滤与查询优化。