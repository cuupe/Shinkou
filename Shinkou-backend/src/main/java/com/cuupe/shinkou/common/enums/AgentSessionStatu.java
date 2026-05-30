package com.cuupe.shinkou.common.enums;

/**
 * 枚举说明
 * | status      | 说明     |
 * | ----------- | -------- |
 * | `PENDING`   | 等待分析 |
 * | `RUNNING`   | 分析中   |
 * | `COMPLETED` | 分析完成 |
 * | `FAILED`    | 分析失败 |
 * | `CANCELLED` | 已取消   |
 */
public enum AgentSessionStatu {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    CANCELLED
}
