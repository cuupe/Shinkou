package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 项目完整信息数据传输对象
 */
@Data
@Accessors(chain = true)
public class ProjectFullDTO {
    private Long id;
    private Long workspaceId;
    private String name;
    private String code;
    private String description;
    private String rootPath;
    private Integer fileCount;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
