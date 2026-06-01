package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分析影响文件实体类，对应数据库analysis_affected_files表
 */
@Data
@TableName("analysis_affected_files")
public class AnalysisAffectedFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String sessionId;
    private Long workspaceId;
    private Long projectId;
    private String filePath;
    private String fileName;
    private String language;
    private Integer lineStart;
    private Integer lineEnd;
    private String reason;
    private BigDecimal confidence;
    private LocalDateTime createdAt;
}