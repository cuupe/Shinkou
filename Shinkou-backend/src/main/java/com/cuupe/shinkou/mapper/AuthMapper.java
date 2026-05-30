package com.cuupe.shinkou.mapper;

import com.cuupe.shinkou.dto.ActivatedWorkspaceDTO;
import com.cuupe.shinkou.dto.UserDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.eneity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AuthMapper {
    @Select("""
        SELECT
            id,
            email,
            password_hash AS passwordHash,
            name,
            avatar_url AS avatarUrl,
            department,
            position,
            status,
            last_login_at AS lastLoginAt,
            created_at AS createdAt,
            updated_at AS updatedAt
        FROM users
        WHERE email = #{email}
        LIMIT 1
        """)
    User findUserByEmail(@Param("email") String email);

    @Select("""
         SELECT
             w.id AS id,
             w.name AS name,
             w.code AS code,
             wm.role AS role,
             w.status AS status
         FROM workspace_members wm
         INNER JOIN workspaces w ON w.id = wm.workspace_id
         WHERE wm.user_id = #{userId}
           AND wm.status = 'ACTIVE'
           AND w.status = 'ACTIVE'
         ORDER BY w.id ASC
    """)
    List<WorkspaceDTO> findActiveWorkspacesByUserId(@Param("userId") Long userId);


    @Update("""
        UPDATE users
        SET last_login_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{userId}
        """)
    int updateLastLoginAt(@Param("userId") Long userId);


    @Select("""
        SELECT
            u.id,
            u.email,
            u.name,
            u.avatar_url AS avatarUrl,
            u.department,
            u.position,
            u.status
        FROM users u
        WHERE id = #{userId}
        """)
    UserDTO findUserById(@Param("userId") Long userId);


    @Select("""
        SELECT
            id,
            name,
            code
        FROM workspaces
        WHERE id = #{workspaceId}
          AND status = 'ACTIVE'
        LIMIT 1
        """)
    ActivatedWorkspaceDTO findWorkspaceById(
            @Param("workspaceId") Long workspaceId
    );


    @Select("""
        SELECT COUNT(1)
        FROM workspace_members
        WHERE workspace_id = #{workspaceId}
          AND user_id = #{userId}
        """)
    int countWorkspaceMember(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId
    );

    @Insert("""
        INSERT INTO users (
            email,
            password_hash,
            name,
            avatar_url,
            department,
            position,
            status,
            last_login_at,
            created_at,
            updated_at
        ) VALUES (
            #{email},
            #{passwordHash},
            #{name},
            #{avatarUrl},
            #{department},
            #{position},
            #{status},
            null,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertUser(User user);

    @Update("""
        UPDATE invitations
        SET status = 'ACCEPTED',
            accepted_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
          AND status = 'PENDING'
        """)
    int markInvitationAccepted(@Param("id") Long id);

    @Insert("""
        INSERT INTO workspace_members (
            workspace_id,
            user_id,
            role,
            status,
            joined_at,
            created_at,
            updated_at
        ) VALUES (
            #{workspaceId},
            #{userId},
            #{role},
            'ACTIVE',
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        )
        """)
    int insertWorkspaceMember(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId,
            @Param("role") String role
    );


    @Update("""
        UPDATE users
        SET password_hash = #{passwordHash},
            name = #{name},
            department = #{department},
            position = #{position},
            status = 'ACTIVE',
            updated_at = CURRENT_TIMESTAMP
        WHERE id = #{id}
        """)
    int activateUser(User user);
}
