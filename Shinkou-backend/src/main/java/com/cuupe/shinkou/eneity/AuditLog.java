package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("audit_logs")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workspaceId;
    private Long userId;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String result;
    private String ipAddress;
    private String userAgent;
    private String requestId;
    private String message;
    /**
     * PostgreSQL JSONB。
     * MVP 阶段先用 String 保存 JSON 字符串。
     */
    private String detail;
    private LocalDateTime createdAt;
}