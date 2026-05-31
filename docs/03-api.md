# 03. API 设计

## 1. 设计原则

Shinkou 是企业内部研发平台，API 设计需要支持：

```text
企业邮箱 + 密码登录
邀请激活账号，不开放普通注册
多工作区
用户登录后选择工作区
所有项目、文件、Agent 分析都归属于某个 workspace
```

MVP 阶段采用：

```text
登录时不传 workspaceCode
登录成功后返回用户加入的 workspaces 列表
如果用户只有一个 workspace，前端直接进入
如果用户有多个 workspace，前端进入工作区选择页
```

业务接口统一使用 workspace 路径前缀：

```http
/api/workspaces/{workspaceId}/...
```

这样可以清楚表达资源归属，并方便后端权限校验。

---

## 2. 通用约定

### 2.1 Base URL

开发环境：

```text
http://localhost:8080
```

### 2.2 认证方式

登录成功后，后端返回 `accessToken`。

后续请求在 Header 中携带：

```http
Authorization: Bearer <accessToken>
```

### 2.3 通用响应格式

MVP 可以直接返回数据对象。

后续推荐统一格式：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {}
}
```

为了开发简洁，本文档中的接口示例默认直接返回业务数据。

### 2.4 通用错误格式

```json
{
  "code": "AUTH_TOKEN_EXPIRED",
  "message": "登录已过期，请重新登录"
}
```

### 2.5 常见错误码

## 通用类

| code                      | 作用                                                         |
| ------------------------- | ------------------------------------------------------------ |
| `SUCCESS`               | 请求成功。接口正常处理并返回数据                             |
| `FAILURE`               | 通用失败。一般不建议大量使用，只有无法归类时使用             |
| `VALIDATION_ERROR`      | 请求参数错误，比如必填字段为空、邮箱格式不正确、密码长度不足 |
| `INTERNAL_SERVER_ERROR` | 服务端未知异常，比如代码异常、数据库异常、未预期错误         |

---

## Auth 认证类

| code                           | 作用                                                            |
| ------------------------------ | --------------------------------------------------------------- |
| `AUTH_INVALID_CREDENTIALS`   | 企业邮箱或密码错误。登录失败时使用                              |
| `AUTH_ACCOUNT_NOT_ACTIVATED` | 账号尚未激活。用户存在但状态还是 `PENDING`                    |
| `AUTH_ACCOUNT_DISABLED`      | 账号已被禁用。用户状态是 `DISABLED`                           |
| `AUTH_ACCOUNT_LOCKED`        | 账号已锁定。比如连续登录失败过多后临时锁定                      |
| `AUTH_TOKEN_INVALID`         | Token 无效。比如 Token 格式错误、签名错误、伪造 Token           |
| `AUTH_TOKEN_EXPIRED`         | Token 已过期。前端应跳转登录页或提示重新登录                    |
| `AUTH_UNAUTHORIZED`          | 未登录或没有认证信息。比如请求没有携带 `Authorization` Header |

区别：

```
AUTH_UNAUTHORIZED      没带 Token
AUTH_TOKEN_INVALID     带了 Token，但 Token 不合法
AUTH_TOKEN_EXPIRED     带了 Token，但 Token 过期
```

---

## Invitation 邀请类

| code                            | 作用                                    |
| ------------------------------- | --------------------------------------- |
| `INVITATION_NOT_FOUND`        | 邀请不存在。根据 token 查询不到邀请记录 |
| `INVITATION_EXPIRED`          | 邀请已过期。`expires_at` 小于当前时间 |
| `INVITATION_REVOKED`          | 邀请已撤销。邀请状态是 `REVOKED`      |
| `INVITATION_ALREADY_ACCEPTED` | 邀请已被使用。邀请状态是 `ACCEPTED`   |

激活账号时常用这些错误码。

---

## Workspace 工作区类

| code                        | 作用                                                          |
| --------------------------- | ------------------------------------------------------------- |
| `WORKSPACE_NOT_FOUND`     | 工作区不存在                                                  |
| `WORKSPACE_ACCESS_DENIED` | 当前用户无权访问该工作区。比如不是该 workspace 的 ACTIVE 成员 |
| `WORKSPACE_DISABLED`      | 工作区已被禁用，不能继续访问或操作                            |

其中 `WORKSPACE_ACCESS_DENIED` 很重要。所有 `/api/workspaces/{workspaceId}/...` 接口都要校验当前用户是否属于该工作区。

---

## Project 项目类

| code                       | 作用                               |
| -------------------------- | ---------------------------------- |
| `PROJECT_NOT_FOUND`      | 项目不存在，或项目不属于当前工作区 |
| `PROJECT_ACCESS_DENIED`  | 当前用户无权访问或操作该项目       |
| `PROJECT_ALREADY_EXISTS` | 同一工作区下项目 code 已存在       |

一般创建项目时，如果 `code` 重复，用：

```
PROJECT_ALREADY_EXISTS
```

查询、上传文件、分析项目时找不到项目，用：

```
PROJECT_NOT_FOUND
```

---

## File 文件类

| code                   | 作用                                                         |
| ---------------------- | ------------------------------------------------------------ |
| `FILE_NOT_FOUND`     | 文件不存在。比如根据 path 找不到项目文件                     |
| `FILE_UPLOAD_FAILED` | 文件上传失败。比如 ZIP 保存失败、文件为空、上传中断          |
| `FILE_READ_FAILED`   | 文件读取失败。比如编码问题、权限问题、磁盘读取异常           |
| `FILE_SEARCH_FAILED` | 文件内容搜索失败。比如搜索工具异常                           |
| `FILE_INVALID_PATH`  | 文件路径非法。比如路径穿越 `../`，或访问了项目目录外的文件 |

`FILE_INVALID_PATH` 很关键，上传 ZIP 和读取文件时都要防止路径穿越。

---

## Agent / AI 类

| code                          | 作用                                                   |
| ----------------------------- | ------------------------------------------------------ |
| `AGENT_SESSION_NOT_FOUND`   | AI 分析会话不存在                                      |
| `AGENT_ANALYZE_FAILED`      | AI 分析失败。比如 Python AI 服务返回失败、分析流程异常 |
| `AGENT_SERVICE_UNAVAILABLE` | Python AI 服务不可用。比如连接超时、服务未启动         |
| `AGENT_TOOL_CALL_FAILED`    | Agent 工具调用失败。比如搜索文件失败、读取文件片段失败 |

# 3. Auth 认证接口

## 3.1 登录

用户使用企业邮箱和密码登录。

登录时不选择工作区，后端返回用户加入的工作区列表。

```http
POST /api/auth/login
```

### Request

```json
{
  "email": "zhangwei@company.com",
  "password": "Password123!"
}
```

### Response

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "user": {
    "id": 1,
    "email": "zhangwei@company.com",
    "name": "张伟",
    "avatarUrl": null,
    "department": "后端研发部",
    "position": "后端开发工程师",
    "status": "ACTIVE"
  },
  "workspaces": [
    {
      "id": 1,
      "name": "Shinkou Engineering",
      "code": "Shinkou-engineering",
      "role": "ADMIN",
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "name": "AI Platform Team",
      "code": "ai-platform",
      "role": "MEMBER",
      "status": "ACTIVE"
    }
  ]
}
```

### 失败示例

```json
{
  "code": "AUTH_INVALID_CREDENTIALS",
  "message": "企业邮箱或密码错误"
}
```

```json
{
  "code": "AUTH_ACCOUNT_NOT_ACTIVATED",
  "message": "账号尚未激活，请通过邀请链接完成账号激活"
}
```

---

## 3.2 获取当前用户

前端刷新页面后，用该接口恢复登录状态和工作区列表。

```http
GET /api/auth/me
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "user": {
    "id": 1,
    "email": "zhangwei@company.com",
    "name": "张伟",
    "avatarUrl": null,
    "department": "后端研发部",
    "position": "后端开发工程师",
    "status": "ACTIVE"
  },
  "workspaces": [
    {
      "id": 1,
      "name": "Shinkou Engineering",
      "code": "Shinkou-engineering",
      "role": "ADMIN",
      "status": "ACTIVE"
    },
    {
      "id": 2,
      "name": "AI Platform Team",
      "code": "ai-platform",
      "role": "MEMBER",
      "status": "ACTIVE"
    }
  ]
}
```

---

## 3.3 退出登录

MVP 可以只在前端删除 token。

如果要记录审计日志，可以保留该接口。

```http
POST /api/auth/logout
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "success": true
}
```

---

## 3.4 查询邀请信息

用户打开激活链接后，前端先调用该接口验证邀请是否有效。

```http
GET /api/invitations/{token}
```

### Response

```json
{
  "token": "inv_demo_admin_token",
  "email": "admin@company.com",
  "name": "系统管理员",
  "department": "研发平台部",
  "position": "平台管理员",
  "role": "ADMIN",
  "status": "PENDING",
  "expiresAt": "2026-06-18T10:00:00",
  "workspace": {
    "id": 1,
    "name": "Shinkou Engineering",
    "code": "Shinkou-engineering"
  }
}
```

### 失败示例

```json
{
  "code": "INVITATION_EXPIRED",
  "message": "邀请链接已过期，请联系管理员重新发送"
}
```

---

## 3.5 激活账号

内部平台不提供普通公开注册。

用户必须通过邀请 token 激活账号。

```http
POST /api/auth/activate
```

### Request

```json
{
  "invitationToken": "inv_demo_admin_token",
  "password": "Password123!",
  "confirmPassword": "Password123!"
}
```

### Response

激活成功后自动登录。

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "tokenType": "Bearer",
  "expiresIn": 7200,
  "user": {
    "id": 1,
    "email": "admin@company.com",
    "name": "系统管理员",
    "avatarUrl": null,
    "department": "研发平台部",
    "position": "平台管理员",
    "status": "ACTIVE"
  },
  "workspaces": [
    {
      "id": 1,
      "name": "Shinkou Engineering",
      "code": "Shinkou-engineering",
      "role": "ADMIN",
      "status": "ACTIVE"
    }
  ],
  "activatedWorkspace": {
    "id": 1,
    "name": "Shinkou Engineering",
    "code": "Shinkou-engineering",
    "role": "ADMIN"
  }
}
```

### 处理逻辑

后端应完成：

```text
1. 根据 invitationToken 查询 invitations
2. 校验邀请存在、未过期、状态为 PENDING
3. 校验密码和确认密码一致
4. 校验密码强度
5. 如果用户不存在，创建 users 记录
6. 如果用户存在且未激活，更新密码并激活
7. 创建 workspace_members 记录
8. 更新 invitations.status = ACCEPTED
9. 写入 accepted_at
10. 返回登录 token 和工作区列表
```

---

# Redis Key 设计

```
auth:token:{userId}:{tokenId}
auth:blacklist:{tokenId}
auth:login_fail:{email}
auth:login_lock:{email}
```

建议 token 里放一个 `jti`，也就是 tokenId。

| Key                               | 作用                      | TTL              |
| --------------------------------- | ------------------------- | ---------------- |
| `auth:token:{userId}:{tokenId}` | 记录用户当前 token 会话   | token 有效期     |
| `auth:blacklist:{tokenId}`      | 退出登录后的 token 黑名单 | token 剩余有效期 |
| `auth:login_fail:{email}`       | 登录失败次数              | 锁定窗口期       |
| `auth:login_lock:{email}`       | 登录临时锁定标记          | 锁定时间         |

# 4. Workspace 工作区接口

## 4.1 获取我的工作区列表

该接口与 `/api/auth/me` 有部分重叠。

如果前端需要单独刷新工作区列表，可以使用该接口。

```http
GET /api/workspaces/my
Authorization: Bearer <accessToken>
```

### Response

```json
[
  {
    "id": 1,
    "name": "Shinkou Engineering",
    "code": "Shinkou-engineering",
    "description": "Shinkou 示例研发团队",
    "role": "ADMIN",
    "status": "ACTIVE"
  },
  {
    "id": 2,
    "name": "AI Platform Team",
    "code": "ai-platform",
    "description": "AI 平台团队",
    "role": "MEMBER",
    "status": "ACTIVE"
  }
]
```

---

## 4.2 获取工作区详情

```http
GET /api/workspaces/{workspaceId}
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "id": 1,
  "name": "Shinkou Engineering",
  "code": "Shinkou-engineering",
  "description": "Shinkou 示例研发团队",
  "status": "ACTIVE",
  "currentUserRole": "ADMIN",
  "createdAt": "2026-05-18T10:00:00",
  "updatedAt": "2026-05-18T10:00:00"
}
```

---

## 4.3 获取工作区成员列表

后续成员管理页使用。

```http
GET /api/workspaces/{workspaceId}/members
Authorization: Bearer <accessToken>
```

### Response

```json
[
  {
    "id": 1,
    "userId": 1,
    "email": "admin@company.com",
    "name": "系统管理员",
    "department": "研发平台部",
    "position": "平台管理员",
    "role": "ADMIN",
    "status": "ACTIVE",
    "joinedAt": "2026-05-18T10:00:00"
  }
]
```

---

## 4.4 创建邀请

MVP 可以先不做页面，用 SQL 预置邀请。

后续成员管理页需要该接口。

```http
POST /api/workspaces/{workspaceId}/invitations
Authorization: Bearer <accessToken>
```

权限：`OWNER` / `ADMIN`

### Request

```json
{
  "email": "liming@company.com",
  "name": "李明",
  "department": "后端研发部",
  "position": "后端开发实习生",
  "role": "MEMBER",
  "expiresInDays": 7
}
```

### Response

```json
{
  "id": 1001,
  "email": "liming@company.com",
  "name": "李明",
  "role": "MEMBER",
  "status": "PENDING",
  "token": "inv_8f3a9c7b2e",
  "activationUrl": "http://localhost:5173/activate?token=inv_8f3a9c7b2e",
  "expiresAt": "2026-05-25T10:00:00"
}
```

---

# 5. Project 项目接口

项目归属于工作区，所以接口使用：

```http
/api/workspaces/{workspaceId}/projects
```

后端每次都需要校验：

```text
当前登录用户是否是该 workspace 的 ACTIVE 成员。
```

---

## 5.1 创建项目

```http
POST /api/workspaces/{workspaceId}/projects
Authorization: Bearer <accessToken>
```

### Request

```json
{
  "name": "Order System Demo",
  "code": "order-system-demo",
  "description": "订单系统示例项目，用于测试需求变更影响分析。"
}
```

### Response

```json
{
  "id": 1001,
  "workspaceId": 1,
  "name": "Order System Demo",
  "code": "order-system-demo",
  "description": "订单系统示例项目，用于测试需求变更影响分析。",
  "rootPath": "./data/workspaces/1/projects/1001",
  "fileCount": 0,
  "status": "ACTIVE",
  "createdBy": 1,
  "createdAt": "2026-05-18T10:00:00",
  "updatedAt": "2026-05-18T10:00:00"
}
```

---

## 5.2 获取项目列表

```http
GET /api/workspaces/{workspaceId}/projects
Authorization: Bearer <accessToken>
```

### Query Params

| 参数        | 必填 | 说明                                    |
| ----------- | ---- | --------------------------------------- |
| `keyword` | 否   | 按项目名称 / code 搜索                  |
| `status`  | 否   | `ACTIVE` / `ARCHIVED` / `DELETED` |

### Response

```json
[
  {
    "id": 1001,
    "workspaceId": 1,
    "name": "Order System Demo",
    "code": "order-system-demo",
    "description": "订单系统示例项目，用于测试需求变更影响分析。",
    "fileCount": 128,
    "status": "ACTIVE",
    "createdBy": 1,
    "createdAt": "2026-05-18T10:00:00",
    "updatedAt": "2026-05-18T10:30:00"
  }
]
```

---

## 5.3 获取项目详情

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "id": 1001,
  "workspaceId": 1,
  "name": "Order System Demo",
  "code": "order-system-demo",
  "description": "订单系统示例项目，用于测试需求变更影响分析。",
  "rootPath": "./data/workspaces/1/projects/1001",
  "fileCount": 128,
  "status": "ACTIVE",
  "createdBy": 1,
  "createdAt": "2026-05-18T10:00:00",
  "updatedAt": "2026-05-18T10:30:00"
}
```

---

## 5.4 更新项目

```http
PATCH /api/workspaces/{workspaceId}/projects/{projectId}
Authorization: Bearer <accessToken>
```

### Request

```json
{
  "name": "Order System",
  "description": "订单系统项目",
  "status": "ACTIVE"
}
```

### Response

```json
{
  "id": 1001,
  "workspaceId": 1,
  "name": "Order System",
  "code": "order-system-demo",
  "description": "订单系统项目",
  "fileCount": 128,
  "status": "ACTIVE",
  "updatedAt": "2026-05-18T11:00:00"
}
```

---

## 5.5 删除项目

MVP 建议软删除。

```http
DELETE /api/workspaces/{workspaceId}/projects/{projectId}
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "success": true
}
```

---

# 6. Project File 文件接口

MVP 第一版先支持 ZIP 上传、文件扫描、文件列表、文本搜索、文件片段读取。

---

## 6.1 上传项目 ZIP

```http
POST /api/workspaces/{workspaceId}/projects/{projectId}/upload-zip
Authorization: Bearer <accessToken>
Content-Type: multipart/form-data
```

### Request

```text
file: order-system-demo.zip
```

### Response

```json
{
  "projectId": 1001,
  "workspaceId": 1,
  "fileCount": 128,
  "status": "uploaded"
}
```

后端处理逻辑：

```text
1. 校验用户是否属于 workspace
2. 校验 project 是否属于 workspace
3. 保存 ZIP
4. 安全解压，禁止路径穿越
5. 过滤 .git、node_modules、target、build 等目录
6. 扫描文件
7. 写入 project_files，后续设计
8. 更新 projects.file_count
```

---

## 6.2 获取项目文件列表

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files
Authorization: Bearer <accessToken>
```

### Response

```json
[
  {
    "id": 1,
    "workspaceId": 1,
    "projectId": 1001,
    "filePath": "backend/src/main/java/com/demo/order/service/OrderService.java",
    "fileName": "OrderService.java",
    "fileExt": ".java",
    "language": ["Java"],
    "size": 3456,
    "indexed": true
  }
]
```

---

## 6.3 获取项目文件树

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/tree
Authorization: Bearer <accessToken>
```

### Response

```json
[
  {
    "name": "backend",
    "path": "backend",
    "type": "directory",
    "children": [
      {
        "name": "src",
        "path": "backend/src",
        "type": "directory",
        "children": []
      }
    ]
  }
]
```

---

## 6.4 搜索文件内容

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/search?keyword=coupon
Authorization: Bearer <accessToken>
```

### Response

```json
[
  {
    "path": "backend/src/main/java/com/demo/order/service/OrderService.java",
    "line": 18,
    "snippet": "Coupon coupon = couponService.validateCoupon(...)"
  },
  {
    "path": "backend/src/main/java/com/demo/order/service/CouponService.java",
    "line": 10,
    "snippet": "public Coupon validateCoupon(...)"
  }
]
```

---

## 6.5 读取文件内容

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/content?path=backend/src/main/java/com/demo/order/service/OrderService.java
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "path": "backend/src/main/java/com/demo/order/service/OrderService.java",
  "language": "Java",
  "content": "package com.demo.order.service;..."
}
```

---

## 6.6 读取文件指定行

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/read-lines?path=backend/src/main/java/com/demo/order/service/OrderService.java&start=1&end=80
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "path": "backend/src/main/java/com/demo/order/service/OrderService.java",
  "start": 1,
  "end": 80,
  "content": "package com.demo.order.service;...",
  "lines": [
    {
      "line": 1,
      "text": "package com.demo.order.service;"
    }
  ]
}
```

---

# 7. Agent 需求影响分析接口

MVP 第一版中，前端调用 Java 主后端，Java 再调用 Python AI 服务。

---

## 7.1 启动需求影响分析

```http
POST /api/workspaces/{workspaceId}/projects/{projectId}/agent/analyze
Authorization: Bearer <accessToken>
```

### Request

```json
{
  "requirement": "给订单系统增加优惠券抵扣功能，用户下单时可以选择一张优惠券，订单金额需要重新计算。"
}
```

### Response

```json
{
  "sessionId": "as_20260518100000001",
  "status": "completed",
  "summary": "该需求涉及订单创建、优惠券校验和支付金额计算流程。",
  "affectedFiles": [
    {
      "path": "backend/src/main/java/com/demo/order/service/OrderService.java",
      "lineStart": 12,
      "lineEnd": 42,
      "reason": "订单金额计算逻辑需要接入优惠券抵扣。",
      "confidence": 0.92
    }
  ],
  "risks": [
    "需要保证订单金额和支付金额一致。",
    "需要处理优惠券过期、不可用、使用门槛不足等异常场景。"
  ],
  "tasks": [
    {
      "title": "修改订单创建流程中的优惠券抵扣逻辑",
      "priority": "P1",
      "description": "在订单创建时校验优惠券并重新计算支付金额。",
      "relatedFiles": [
        "backend/src/main/java/com/demo/order/service/OrderService.java"
      ]
    }
  ],
  "steps": [
    {
      "toolName": "search_text",
      "arguments": {
        "keyword": "coupon"
      },
      "status": "success",
      "latencyMs": 120
    }
  ]
}
```

---

## 7.2 获取 Agent 会话详情

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/agent-sessions/{sessionId}
Authorization: Bearer <accessToken>
```

### Response

```json
{
  "sessionId": "as_20260518100000001",
  "workspaceId": 1,
  "projectId": 1001,
  "requirement": "给订单系统增加优惠券抵扣功能，用户下单时可以选择一张优惠券，订单金额需要重新计算。",
  "status": "completed",
  "summary": "该需求涉及订单创建、优惠券校验和支付金额计算流程。",
  "createdAt": "2026-05-18T10:00:00",
  "updatedAt": "2026-05-18T10:00:08"
}
```

---

## 7.3 获取 Agent 工具调用记录

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/agent-sessions/{sessionId}/tool-calls
Authorization: Bearer <accessToken>
```

### Response

```json
[
  {
    "id": 1,
    "sessionId": "as_20260518100000001",
    "toolName": "search_text",
    "arguments": {
      "project_id": 1001,
      "keyword": "coupon"
    },
    "result": {
      "hitCount": 8
    },
    "status": "success",
    "latencyMs": 120,
    "createdAt": "2026-05-18T10:00:01"
  }
]
```

---

# 8. Python AI Service 内部接口

该接口只供 Java 主后端调用。

MVP 阶段可以不对外暴露。

---

## 8.1 需求分析

```http
POST /agent/analyze
```

### Request

```json
{
  "workspace_id": 1,
  "project_id": 1001,
  "requirement": "给订单系统增加优惠券抵扣功能，用户下单时可以选择一张优惠券，订单金额需要重新计算。"
}
```

### Response

```json
{
  "summary": "该需求涉及订单创建、优惠券校验和支付金额计算流程。",
  "affected_files": [
    {
      "path": "backend/src/main/java/com/demo/order/service/OrderService.java",
      "line_start": 12,
      "line_end": 42,
      "reason": "订单金额计算逻辑需要接入优惠券抵扣。",
      "confidence": 0.92
    }
  ],
  "risks": [],
  "tasks": [],
  "steps": []
}
```

---

# 9. 前端路由建议

登录与激活：

```text
/login
/activate?token=xxx
/workspace-select
```

工作区内页面：

```text
/workspaces/:workspaceId/projects
/workspaces/:workspaceId/projects/:projectId/files
/workspaces/:workspaceId/projects/:projectId/analysis
/workspaces/:workspaceId/projects/:projectId/tasks
/workspaces/:workspaceId/projects/:projectId/reports
```

登录后：

```text
如果用户只有一个 workspace：直接进入 /workspaces/:workspaceId/projects
如果用户有多个 workspace：进入 /workspace-select
```

---

# 10. MVP 优先实现接口

第一阶段只需要实现这些：

```http
POST /api/auth/login
GET  /api/auth/me
GET  /api/invitations/{token}
POST /api/auth/activate
GET  /api/workspaces/my
POST /api/workspaces/{workspaceId}/projects
GET  /api/workspaces/{workspaceId}/projects
GET  /api/workspaces/{workspaceId}/projects/{projectId}
POST /api/workspaces/{workspaceId}/projects/{projectId}/upload-zip
GET  /api/workspaces/{workspaceId}/projects/{projectId}/files
GET  /api/workspaces/{workspaceId}/projects/{projectId}/files/search
GET  /api/workspaces/{workspaceId}/projects/{projectId}/files/read-lines
POST /api/workspaces/{workspaceId}/projects/{projectId}/agent/analyze
```

其余接口后续逐步补充。
