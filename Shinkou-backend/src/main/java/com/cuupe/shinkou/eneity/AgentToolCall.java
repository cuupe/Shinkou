package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent工具调用实体类，对应数据库agent_tool_calls表
 */
@Data
@TableName("agent_tool_calls")
public class AgentToolCall {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Long workspaceId;
    private Long projectId;
    private String toolName;
    /**
     * PostgreSQL JSONB。
     * MVP 阶段先用 String 保存 JSON 字符串。
     */
    private String arguments;
    /**
     * PostgreSQL JSONB。
     * MVP 阶段先用 String 保存 JSON 字符串。
     */
    private String result;
    private String status;
    private Integer latencyMs;
    private String errorMessage;
    private LocalDateTime createdAt;
}