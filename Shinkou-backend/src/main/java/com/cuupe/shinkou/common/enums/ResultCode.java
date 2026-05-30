package com.cuupe.shinkou.common.enums;

/**
 * 枚举说明
 * | code                          | 说明               |
 * | ----------------------------- | ------------------ |
 * | `AUTH_INVALID_CREDENTIALS`    | 企业邮箱或密码错误  |
 * | `AUTH_ACCOUNT_NOT_ACTIVATED`  | 账号尚未激活        |
 * | `AUTH_ACCOUNT_DISABLED`       | 账号已被禁用        |
 * | `AUTH_TOKEN_INVALID`          | Token 无效         |
 * | `AUTH_TOKEN_EXPIRED`          | Token 已过期       |
 * | `INVITATION_NOT_FOUND`        | 邀请不存在         |
 * | `INVITATION_EXPIRED`          | 邀请已过期         |
 * | `INVITATION_REVOKED`          | 邀请已撤销         |
 * | `INVITATION_ALREADY_ACCEPTED` | 邀请已被使用        |
 * | `WORKSPACE_ACCESS_DENIED`     | 无权访问该工作区    |
 * | `PROJECT_NOT_FOUND`           | 项目不存在         |
 * | `FILE_NOT_FOUND`              | 文件不存在         |
 * | `VALIDATION_ERROR`            | 请求参数错误       |
 */
public enum ResultCode {
    // 通用
    SUCCESS,
    FAILURE,
    VALIDATION_ERROR,
    INTERNAL_SERVER_ERROR,

    // Auth 认证
    AUTH_INVALID_CREDENTIALS,
    AUTH_ACCOUNT_NOT_ACTIVATED,
    AUTH_ACCOUNT_DISABLED,
    AUTH_ACCOUNT_LOCKED,
    AUTH_TOKEN_INVALID,
    AUTH_TOKEN_EXPIRED,
    AUTH_UNAUTHORIZED,

    // Invitation 邀请
    INVITATION_NOT_FOUND,
    INVITATION_EXPIRED,
    INVITATION_REVOKED,
    INVITATION_ALREADY_ACCEPTED,

    // Workspace 工作区
    WORKSPACE_NOT_FOUND,
    WORKSPACE_ACCESS_DENIED,
    WORKSPACE_DISABLED,

    // Project 项目
    PROJECT_NOT_FOUND,
    PROJECT_ACCESS_DENIED,
    PROJECT_ALREADY_EXISTS,

    // File 文件
    FILE_NOT_FOUND,
    FILE_UPLOAD_FAILED,
    FILE_READ_FAILED,
    FILE_SEARCH_FAILED,
    FILE_INVALID_PATH,

    // Agent / AI
    AGENT_SESSION_NOT_FOUND,
    AGENT_ANALYZE_FAILED,
    AGENT_SERVICE_UNAVAILABLE,
    AGENT_TOOL_CALL_FAILED
}
