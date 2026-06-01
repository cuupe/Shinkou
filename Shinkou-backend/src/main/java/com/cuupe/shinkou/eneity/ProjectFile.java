package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 项目文件实体类，对应数据库project_files表
 */
@Data
@TableName("project_files")
public class ProjectFile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workspaceId;
    private Long projectId;
    private String filePath;
    private String fileName;
    private String fileExt;
    private String language;
    private Long size;
    private String checksum;
    private Boolean indexed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}