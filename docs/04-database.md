# Database 表设计

## 设计原则

DevMind 按企业内部研发平台设计，数据库需要支持：

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
DevMind Engineering
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
| `name`        | `VARCHAR(255)` | 工作区名称，例如 `DevMind Engineering`     |
| `code`        | `VARCHAR(100)` | 工作区唯一编码，例如 `devmind-engineering` |
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
| 张伟 | DevMind Engineering | ADMIN  |
| 张伟 | AI Platform Team    | MEMBER |
| 李明 | DevMind Engineering | MEMBER |

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

DevMind 是企业内部平台，不开放普通注册。用户需要通过邀请链接激活账号。

邀请记录的含义是：

```text
邀请某个企业邮箱加入某个 workspace，并指定激活后的角色。
```

示例：

```text
邀请 liming@company.com 加入 DevMind Engineering，角色 MEMBER
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
DevMind Engineering
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
    'DevMind Engineering',
    'devmind-engineering',
    'DevMind 示例研发团队',
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