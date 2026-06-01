package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分析风险实体类，对应数据库analysis_risks表
 */
@Data
@TableName("analysis_risks")
public class AnalysisRisk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Long workspaceId;
    private Long projectId;
    private String riskLevel;
    private String description;
    private String suggestion;
    private LocalDateTime createdAt;
}
