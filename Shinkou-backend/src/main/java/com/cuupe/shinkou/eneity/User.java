package com.cuupe.shinkou.eneity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户实体类，对应数据库users表
 */
@Data
@TableName("users")
@Accessors(chain = true)
public class User {
    /**
     * PostgreSQL BIGSERIAL
     * Java 用 Long
     * 数据库自增，MyBatis-Plus 用 IdType.AUTO
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    private String email;
    private String passwordHash;
    private String name;
    private String avatarUrl;
    private String department;
    private String position;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
