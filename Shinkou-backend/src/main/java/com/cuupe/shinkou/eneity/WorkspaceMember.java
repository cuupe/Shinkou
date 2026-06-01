package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作区成员实体类，对应数据库workspace_members表
 */
@Data
@TableName("workspace_members")
public class WorkspaceMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long workspaceId;
    private Long userId;
    private String role;
    private String status;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
