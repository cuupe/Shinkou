package com.cuupe.shinkou.common.enums;


/**
 * 枚举说明
 * | role     | 说明                             |
 * | -------- | -------------------------------- |
 * | `OWNER`  | 工作区所有者，最高权限           |
 * | `ADMIN`  | 管理员，可以邀请成员、管理项目   |
 * | `MEMBER` | 普通成员，可以使用项目和发起分析 |
 */
public enum WorkspaceMemberRole {
    OWNER,
    ADMIN,
    MEMBER
}
