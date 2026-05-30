package com.cuupe.shinkou.common.enums;

/**
 * 枚举说明
 * | status     | 说明                 |
 * | ---------- | -------------------- |
 * | `ACTIVE`   | 正常项目             |
 * | `ARCHIVED` | 已归档，不再主动分析 |
 * | `DELETED`  | 软删除               |
 */
public enum ProjectStatu {
    ACTIVE,
    ARCHIVED,
    DELETED
}
