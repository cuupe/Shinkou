package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("analysis_task_drafts")
public class AnalysisTaskDraft {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Long workspaceId;
    private Long projectId;
    private String title;
    private String priority;
    private String description;
    /**
     * PostgreSQL JSONB。
     * MVP 阶段先用 String 保存 JSON 字符串。
     *
     * 示例：
     * [
     *   "backend/src/main/java/com/demo/order/service/OrderService.java",
     *   "backend/src/main/java/com/demo/order/service/CouponService.java"
     * ]
     */
    private String relatedFiles;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
