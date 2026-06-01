# 03. API 设计

> 本次调整说明：
>
> - 认证方式从“前端保存 `accessToken` 并通过 `Authorization: Bearer` 发送”调整为“后端写入 `HttpOnly Cookie`，浏览器自动携带”。
> - 原有 Bearer Token 说明未完全删除，作为兼容/调试方式保留。
> - 登录、激活接口的响应示例不再直接暴露 `accessToken`。
> - 退出登录从“前端删除 token”调整为“后端删除 Redis 会话并清除 Cookie”。
> - 新增 Cookie、CORS、CSRF、密码规则、项目创建校验等说明。
> - 调整了部分 Markdown 层级和小节排版，使“通用约定 / 错误码 / Auth / Redis / Workspace / Project”边界更清晰。

---

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

MVP 当前推荐使用 **HttpOnly Cookie** 保存登录态。

登录或激活成功后，后端通过响应头写入 Cookie：

```http
Set-Cookie: access_token=<jwt>; HttpOnly; Path=/; Max-Age=7200; SameSite=Lax
```

后续请求不需要前端手动传 `accessToken`，浏览器会自动携带：

```http
Cookie: access_token=<jwt>
```

前端请求需要开启携带 Cookie：

```js
fetch("http://localhost:8080/api/auth/me", {
  credentials: "include"
});
```

Axios 可以统一配置：

```js
axios.defaults.withCredentials = true;
```

兼容说明：

```text
后端可以继续兼容 # 需要登录：浏览器自动携带 access_token Cookie，
用于 Postman、curl、Apifox 等调试场景。
但浏览器前端不再直接读取或保存 accessToken。
```

### 2.3 通用响应格式

接口统一使用包装响应：

```json
{
  "code": "SUCCESS",
  "message": "success",
  "data": {}
}
```

说明：

```text
code    业务状态码
message 给前端展示或调试的简短信息
data    实际业务数据，可以是对象、数组或 null
```

后续接口示例默认展示 `data` 内的业务结构；实际 HTTP 响应应外层包裹 `Result`。

### 2.4 通用错误格式

```json
{
  "code": "AUTH_TOKEN_EXPIRED",
  "message": "登录已过期，请重新登录",
  "data": null
}
```

### 2.5 Cookie / CORS / CSRF 约定

使用 Cookie 认证时，需要注意：

```text
1. Cookie 名称：access_token
2. Cookie 属性：HttpOnly、Path=/、Max-Age=token 有效期
3. 本地开发：Secure=false
4. HTTPS 环境：Secure=true
5. SameSite：公司内部同站点系统建议 Lax 或 Strict
```

如果前后端不同端口，例如：

```text
前端：http://localhost:5173
后端：http://localhost:8080
```

后端 CORS 需要允许携带凭证：

```text
Access-Control-Allow-Credentials: true
Access-Control-Allow-Origin: http://localhost:5173
```

前端请求需要：

```js
credentials: "include"
```

CSRF 防护建议：

```text
1. 不使用 GET 做创建、修改、删除等写操作
2. Cookie 使用 SameSite=Lax 或 SameSite=Strict
3. 重要写操作可以校验 Origin / Referer
4. 如果未来跨站点部署并使用 SameSite=None，则建议增加 CSRF Token
```

兼容调试：

```text
本地 curl / Apifox / Postman 可以继续使用 # 需要登录：浏览器自动携带 access_token Cookie。
浏览器前端推荐只使用 Cookie。
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
| `AUTH_UNAUTHORIZED`          | 未登录或没有认证信息。比如请求没有携带有效 Cookie，或兼容模式下没有携带 `Authorization` Header |

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

| `WORKSPACE_MEMBER_NOT_FOUND` | 工作区成员不存在或已被移出 |
| `WORKSPACE_OWNER_REQUIRED` | 操作需要工作区 OWNER 权限 |
| `WORKSPACE_LAST_OWNER_REQUIRED` | 不能移除或降级最后一个 OWNER |

其中 `WORKSPACE_ACCESS_DENIED` 很重要。所有 `/api/workspaces/{workspaceId}/...` 接口都要校验当前用户是否属于该工作区。

---

---

## System Admin / User 用户管理类

| code | 作用 |
| ---- | ---- |
| `ADMIN_REQUIRED` | 当前用户不是系统管理员，不能访问系统管理接口 |
| `USER_NOT_FOUND` | 用户不存在 |
| `USER_ALREADY_DISABLED` | 用户已经被禁用 |
| `USER_CANNOT_DISABLE_SELF` | 不能禁用当前登录用户自己 |

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

登录成功后，后端通过 `Set-Cookie` 写入 `access_token`，响应体不直接暴露 token。

```http
Set-Cookie: access_token=<jwt>; HttpOnly; Path=/; Max-Age=7200; SameSite=Lax
```

```json
{
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
  "message": "企业邮箱或密码错误",
  "data": null
}
```

```json
{
  "code": "AUTH_ACCOUNT_NOT_ACTIVATED",
  "message": "账号尚未激活，请通过邀请链接完成账号激活",
  "data": null
}
```

---

## 3.2 获取当前用户

前端刷新页面后，用该接口恢复登录状态和工作区列表。

```http
GET /api/auth/me
# 需要登录：浏览器自动携带 access_token Cookie
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

退出登录由后端完成：

```text
1. 从 Cookie 或兼容 Header 中解析当前 accessToken
2. 解析 JWT，取得 userId 和 tokenId
3. 删除 Redis 中的 auth:token:{userId}:{tokenId}
4. 清除浏览器中的 access_token Cookie
```

```http
POST /api/auth/logout
# 需要登录：浏览器自动携带 access_token Cookie
```

### Response Header

```http
Set-Cookie: access_token=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax
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
  "message": "邀请链接已过期，请联系管理员重新发送",
  "data": null
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

### 密码规则

```text
8-32 个字符
必须包含大写字母
必须包含小写字母
必须包含数字
必须包含特殊字符，例如 !@#$%^&*
```

### Response

激活成功后自动登录。后端通过 `Set-Cookie` 写入 `access_token`，响应体不直接暴露 token。

```http
Set-Cookie: access_token=<jwt>; HttpOnly; Path=/; Max-Age=7200; SameSite=Lax
```

```json
{
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
10. 写入登录 Cookie，并返回用户信息和工作区列表
```

---

# Redis Key 设计

Cookie 只负责在浏览器和后端之间携带 JWT。后端仍然需要 Redis 判断 JWT 是否处于有效会话中。

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


# 4. 权限与删除策略

企业内部系统建议优先使用软删除和状态流转，不建议直接物理删除核心业务数据。

## 4.1 角色层级

工作区内角色建议：

```text
OWNER > ADMIN > MEMBER
```

基础权限建议：

| 操作 | OWNER | ADMIN | MEMBER |
| ---- | :---: | :---: | :----: |
| 查看工作区 | 是 | 是 | 是 |
| 修改工作区信息 | 是 | 可选 | 否 |
| 归档 / 删除工作区 | 是 | 否 | 否 |
| 创建邀请 | 是 | 是 | 否 |
| 修改成员角色 | 是 | 否 | 否 |
| 移除 MEMBER | 是 | 是 | 否 |
| 移除 ADMIN | 是 | 否 | 否 |
| 移除 OWNER | 否，需先转让 | 否 | 否 |
| 创建项目 | 是 | 是 | 可选 |
| 更新项目 | 是 | 是 | 可选 |
| 删除 / 归档项目 | 是 | 是 | 可选 |
| 查看项目 | 是 | 是 | 是 |

## 4.2 软删除策略

| 资源 | 推荐做法 | 原因 |
| ---- | -------- | ---- |
| Workspace | `status = DELETED` 或先 `ARCHIVED` | 保留项目、成员、邀请、分析记录 |
| Project | `status = DELETED` 或先 `ARCHIVED` | 保留文件与历史分析结果 |
| Workspace Member | `status = REMOVED` | 保留成员加入和移出记录 |
| User | `status = DISABLED` | 用户是全局账号，可能关联多个工作区 |

## 4.3 项目成员策略

MVP 阶段暂不建议新增项目成员表。项目权限继承工作区成员权限：

```text
能访问 workspace 的 ACTIVE 成员，可以查看该 workspace 下的项目。
项目的创建、更新、删除由 workspace role 控制。
```

后续如果需要“某个项目只允许部分工作区成员访问”，再新增 `project_members` 表和项目级成员接口。

---

# 5. Workspace 工作区接口

## 5.1 获取我的工作区列表

该接口与 `/api/auth/me` 有部分重叠。

如果前端需要单独刷新工作区列表，可以使用该接口。

```http
GET /api/workspaces/my
# 需要登录：浏览器自动携带 access_token Cookie
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

## 5.2 获取工作区详情

```http
GET /api/workspaces/{workspaceId}
# 需要登录：浏览器自动携带 access_token Cookie
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

## 5.3 获取工作区成员列表

后续成员管理页使用。

```http
GET /api/workspaces/{workspaceId}/members
# 需要登录：浏览器自动携带 access_token Cookie
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

## 5.4 创建邀请

MVP 可以先不做页面，用 SQL 预置邀请。

后续成员管理页需要该接口。

```http
POST /api/workspaces/{workspaceId}/invitations
# 需要登录：浏览器自动携带 access_token Cookie
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


## 5.5 更新工作区信息

用于修改工作区的展示信息。工作区 `code` 建议创建后保持稳定，除非后端已经处理好所有关联路径、邀请链接和缓存影响。

```http
PATCH /api/workspaces/{workspaceId}
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER`，或允许 `ADMIN` 修改非关键字段。

### Request

```json
{
  "name": "Shinkou Engineering Platform",
  "description": "企业内部研发效能平台"
}
```

### Response

```json
{
  "id": 1,
  "name": "Shinkou Engineering Platform",
  "code": "shinkou-engineering",
  "description": "企业内部研发效能平台",
  "status": "ACTIVE",
  "updatedAt": "2026-05-18T11:00:00"
}
```

---

## 5.6 归档工作区

归档表示工作区暂时不可继续新增项目或成员，但历史数据仍然保留。相比删除，归档更适合企业内部系统。

```http
PATCH /api/workspaces/{workspaceId}/archive
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER`

### Response

```json
{
  "success": true
}
```

### 处理逻辑

```text
1. 校验当前用户是该 workspace 的 OWNER
2. 校验 workspace 当前状态是 ACTIVE
3. 更新 workspaces.status = ARCHIVED
4. 后续创建项目、创建邀请等写操作应拒绝
5. 历史项目、文件和分析记录仍可按业务规则只读访问
```

---

## 5.7 恢复工作区

```http
PATCH /api/workspaces/{workspaceId}/restore
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER`

### Response

```json
{
  "success": true
}
```

---

## 5.8 删除工作区

企业内部系统不建议物理删除工作区。该接口执行软删除，将 `workspaces.status` 更新为 `DELETED`。

```http
DELETE /api/workspaces/{workspaceId}
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER`

### Response

```json
{
  "success": true
}
```

### 处理逻辑

```text
1. 校验当前用户是该 workspace 的 OWNER
2. 校验 workspace 存在且未删除
3. 可选：校验是否允许删除含有项目/成员/分析记录的工作区
4. 更新 workspaces.status = DELETED
5. 后续 /api/workspaces/{workspaceId}/... 默认返回 WORKSPACE_NOT_FOUND 或 WORKSPACE_ACCESS_DENIED
```

### 注意事项

```text
不要直接 DELETE FROM workspaces。
工作区下面通常有关联的 projects、workspace_members、invitations、project_files、agent_sessions 等数据。
软删除可以保留审计和恢复空间。
```

---

## 5.9 修改成员角色

用于工作区成员管理页调整成员权限。

```http
PATCH /api/workspaces/{workspaceId}/members/{userId}/role
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER`

### Request

```json
{
  "role": "ADMIN"
}
```

`role` 可选值：

```text
OWNER / ADMIN / MEMBER
```

### Response

```json
{
  "userId": 2,
  "workspaceId": 1,
  "role": "ADMIN",
  "status": "ACTIVE",
  "updatedAt": "2026-05-18T11:00:00"
}
```

### 保护规则

```text
1. 不能把最后一个 OWNER 降级
2. 不能把自己降级为非 OWNER，导致工作区没有 OWNER
3. ADMIN 不允许修改其他成员角色
4. MEMBER 不允许修改成员角色
```

---

## 5.10 移出工作区成员

用于将其他用户从工作区移出。不要删除 users 记录，只更新 workspace_members 关系。

```http
DELETE /api/workspaces/{workspaceId}/members/{userId}
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER` / `ADMIN`

### Response

```json
{
  "success": true
}
```

### 处理逻辑

```text
1. 校验当前用户属于该 workspace
2. 校验当前用户有成员管理权限
3. 查询目标用户在 workspace 中的角色和状态
4. 校验不能移除最后一个 OWNER
5. 校验不能越权移除同级或更高级角色
6. 更新 workspace_members.status = REMOVED
7. 可选：删除目标用户在该 workspace 相关的缓存权限
```

### 权限规则建议

| 当前操作者 | 可移除对象 |
| ---------- | ---------- |
| `OWNER`  | `ADMIN` / `MEMBER` |
| `ADMIN`  | `MEMBER` |
| `MEMBER` | 无 |

---

## 5.11 主动退出工作区

用户主动离开某个工作区。

```http
POST /api/workspaces/{workspaceId}/leave
# 需要登录：浏览器自动携带 access_token Cookie
```

### Response

```json
{
  "success": true
}
```

### 保护规则

```text
1. 最后一个 OWNER 不能退出工作区
2. 用户退出后，workspace_members.status = REMOVED
3. 用户再次访问该 workspace 时返回 WORKSPACE_ACCESS_DENIED
```

---

# 6. Project 项目接口

项目归属于工作区，所以接口使用：

```http
/api/workspaces/{workspaceId}/projects
```

后端每次都需要校验：

```text
当前登录用户是否是该 workspace 的 ACTIVE 成员。
```

---

## 6.1 创建项目

```http
POST /api/workspaces/{workspaceId}/projects
# 需要登录：浏览器自动携带 access_token Cookie
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
  "rootPath": null,
  "fileCount": 0,
  "status": "ACTIVE",
  "createdBy": 1,
  "createdAt": "2026-05-18T10:00:00",
  "updatedAt": "2026-05-18T10:00:00"
}
```

### 处理逻辑

```text
1. 从 Cookie 认证信息中取得当前 userId
2. 校验当前用户是否是该 workspace 的 ACTIVE 成员
3. 校验当前用户是否有创建项目权限
4. 校验 name、code 等参数
5. 校验同一工作区下 code 不重复
6. 创建 projects 记录
7. rootPath 初始为 null，文件上传成功后由后端生成并更新
8. fileCount 初始为 0
```

### 重复 code

如果同一工作区下已经存在相同 `code`，返回：

```json
{
  "code": "PROJECT_ALREADY_EXISTS",
  "message": "该工作区下已存在相同 code 的项目",
  "data": null
}
```

数据库层应保留唯一约束：

```sql
CONSTRAINT uk_project_workspace_code UNIQUE (workspace_id, code)
```


---

## 6.2 获取项目列表

```http
GET /api/workspaces/{workspaceId}/projects
# 需要登录：浏览器自动携带 access_token Cookie
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

## 6.3 获取项目详情

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}
# 需要登录：浏览器自动携带 access_token Cookie
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

## 6.4 更新项目

```http
PATCH /api/workspaces/{workspaceId}/projects/{projectId}
# 需要登录：浏览器自动携带 access_token Cookie
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

## 6.5 删除项目

MVP 建议软删除。

```http
DELETE /api/workspaces/{workspaceId}/projects/{projectId}
# 需要登录：浏览器自动携带 access_token Cookie
```

### Response

```json
{
  "success": true
}
```

---


## 6.6 归档项目

归档项目用于隐藏或停止维护项目，但保留文件、分析记录和历史数据。

```http
PATCH /api/workspaces/{workspaceId}/projects/{projectId}/archive
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER` / `ADMIN`，或项目创建者可选。

### Response

```json
{
  "success": true
}
```

### 处理逻辑

```text
1. 校验用户属于 workspace
2. 校验 project 属于 workspace
3. 校验用户有项目管理权限
4. 更新 projects.status = ARCHIVED
```

---

## 6.7 恢复项目

```http
PATCH /api/workspaces/{workspaceId}/projects/{projectId}/restore
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：`OWNER` / `ADMIN`

### Response

```json
{
  "success": true
}
```

---


# 7. System Admin 用户管理接口

该章节用于系统级管理员管理全局用户账号。

注意：`users` 是全局账号，不属于单个 workspace。工作区内“删除用户”应使用：

```http
DELETE /api/workspaces/{workspaceId}/members/{userId}
```

不要直接删除 `users` 记录。企业内部系统推荐禁用账号，而不是物理删除账号。

---

## 7.1 获取用户列表

```http
GET /api/admin/users
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：系统管理员。

### Query Params

| 参数 | 必填 | 说明 |
| ---- | ---- | ---- |
| `keyword` | 否 | 按邮箱、姓名、部门搜索 |
| `status` | 否 | `PENDING` / `ACTIVE` / `DISABLED` / `LOCKED` |
| `page` | 否 | 页码，从 1 开始 |
| `pageSize` | 否 | 每页数量 |

### Response

```json
{
  "items": [
    {
      "id": 1,
      "email": "admin@company.com",
      "name": "系统管理员",
      "department": "研发平台部",
      "position": "平台管理员",
      "status": "ACTIVE",
      "createdAt": "2026-05-18T10:00:00",
      "updatedAt": "2026-05-18T10:00:00"
    }
  ],
  "page": 1,
  "pageSize": 20,
  "total": 1
}
```

---

## 7.2 获取用户详情

```http
GET /api/admin/users/{userId}
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：系统管理员。

### Response

```json
{
  "id": 1,
  "email": "admin@company.com",
  "name": "系统管理员",
  "department": "研发平台部",
  "position": "平台管理员",
  "status": "ACTIVE",
  "workspaces": [
    {
      "id": 1,
      "name": "Shinkou Engineering",
      "code": "shinkou-engineering",
      "role": "OWNER",
      "memberStatus": "ACTIVE"
    }
  ]
}
```

---

## 7.3 更新用户基础信息

```http
PATCH /api/admin/users/{userId}
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：系统管理员。

### Request

```json
{
  "name": "张伟",
  "department": "后端研发部",
  "position": "后端开发工程师",
  "avatarUrl": null
}
```

### Response

```json
{
  "id": 1,
  "email": "zhangwei@company.com",
  "name": "张伟",
  "department": "后端研发部",
  "position": "后端开发工程师",
  "status": "ACTIVE",
  "updatedAt": "2026-05-18T11:00:00"
}
```

---

## 7.4 禁用用户

禁用用户会让该账号无法继续登录。建议同时删除该用户所有 Redis 登录 token，让用户立即下线。

```http
PATCH /api/admin/users/{userId}/disable
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：系统管理员。

### Response

```json
{
  "success": true
}
```

### 处理逻辑

```text
1. 校验当前操作者是系统管理员
2. 校验不能禁用自己，除非有额外确认机制
3. 更新 users.status = DISABLED
4. 删除该 userId 下所有 auth:token:{userId}:* Redis token
5. 后续该用户访问接口返回 AUTH_ACCOUNT_DISABLED 或 AUTH_UNAUTHORIZED
```

---

## 7.5 启用用户

```http
PATCH /api/admin/users/{userId}/enable
# 需要登录：浏览器自动携带 access_token Cookie
```

权限：系统管理员。

### Response

```json
{
  "success": true
}
```

---

## 7.6 用户删除策略

MVP 阶段不提供物理删除用户接口。

推荐策略：

```text
1. 工作区内移除用户：DELETE /api/workspaces/{workspaceId}/members/{userId}
2. 全局停用账号：PATCH /api/admin/users/{userId}/disable
3. 不做 DELETE /api/admin/users/{userId} 物理删除
```

原因：

```text
用户可能关联多个 workspace、项目、邀请、审计日志和分析记录。
物理删除会破坏历史数据追溯。
```

---

# 8. Project File 文件接口

MVP 第一版先支持 ZIP 上传、文件扫描、文件列表、文本搜索、文件片段读取。

---

## 8.1 上传项目 ZIP

```http
POST /api/workspaces/{workspaceId}/projects/{projectId}/upload-zip
# 需要登录：浏览器自动携带 access_token Cookie
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

## 8.2 获取项目文件列表

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files
# 需要登录：浏览器自动携带 access_token Cookie
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

## 8.3 获取项目文件树

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/tree
# 需要登录：浏览器自动携带 access_token Cookie
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

## 8.4 搜索文件内容

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/search?keyword=coupon
# 需要登录：浏览器自动携带 access_token Cookie
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

## 8.5 读取文件内容

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/content?path=backend/src/main/java/com/demo/order/service/OrderService.java
# 需要登录：浏览器自动携带 access_token Cookie
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

## 8.6 读取文件指定行

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/files/read-lines?path=backend/src/main/java/com/demo/order/service/OrderService.java&start=1&end=80
# 需要登录：浏览器自动携带 access_token Cookie
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

# 9. Agent 需求影响分析接口

MVP 第一版中，前端调用 Java 主后端，Java 再调用 Python AI 服务。

---

## 9.1 启动需求影响分析

```http
POST /api/workspaces/{workspaceId}/projects/{projectId}/agent/analyze
# 需要登录：浏览器自动携带 access_token Cookie
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

## 9.2 获取 Agent 会话详情

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/agent-sessions/{sessionId}
# 需要登录：浏览器自动携带 access_token Cookie
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

## 9.3 获取 Agent 工具调用记录

```http
GET /api/workspaces/{workspaceId}/projects/{projectId}/agent-sessions/{sessionId}/tool-calls
# 需要登录：浏览器自动携带 access_token Cookie
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

# 10. Python AI Service 内部接口

该接口只供 Java 主后端调用。

MVP 阶段可以不对外暴露。

---

## 10.1 需求分析

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

# 11. 前端请求约定

浏览器前端使用 Cookie 登录态，不直接保存 `accessToken`。

## 11.1 fetch 示例

```js
await fetch("http://localhost:8080/api/auth/login", {
  method: "POST",
  credentials: "include",
  headers: {
    "Content-Type": "application/json"
  },
  body: JSON.stringify({
    email,
    password
  })
});
```

登录后的请求：

```js
await fetch("http://localhost:8080/api/auth/me", {
  credentials: "include"
});
```

## 11.2 axios 示例

```js
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true
});
```

## 11.3 不再推荐的前端行为

```text
不要把 accessToken 存在 localStorage
不要从登录响应中读取 accessToken
不要在浏览器前端手动拼 Authorization: Bearer
```

---

# 12. 前端路由建议

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

# 13. MVP 优先实现接口

第一阶段只需要实现这些：

```http
POST /api/auth/login
GET  /api/auth/me
POST /api/auth/logout
GET  /api/invitations/{token}
POST /api/auth/activate
GET  /api/workspaces/my
POST /api/workspaces/{workspaceId}/projects
GET  /api/workspaces/{workspaceId}/projects
GET  /api/workspaces/{workspaceId}/projects/{projectId}
DELETE /api/workspaces/{workspaceId}/projects/{projectId}
DELETE /api/workspaces/{workspaceId}/members/{userId}
POST /api/workspaces/{workspaceId}/leave
POST /api/workspaces/{workspaceId}/projects/{projectId}/upload-zip
GET  /api/workspaces/{workspaceId}/projects/{projectId}/files
GET  /api/workspaces/{workspaceId}/projects/{projectId}/files/search
GET  /api/workspaces/{workspaceId}/projects/{projectId}/files/read-lines
POST /api/workspaces/{workspaceId}/projects/{projectId}/agent/analyze
```

管理后台如果进入 MVP，再补充：

```http
PATCH /api/workspaces/{workspaceId}/members/{userId}/role
PATCH /api/workspaces/{workspaceId}/archive
DELETE /api/workspaces/{workspaceId}
GET   /api/admin/users
PATCH /api/admin/users/{userId}/disable
PATCH /api/admin/users/{userId}/enable
```

其余接口后续逐步补充。
