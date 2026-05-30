package com.cuupe.shinkou.mapper;

import com.cuupe.shinkou.dto.ProjectDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface WorkspaceMapper {

    @Select("""
        SELECT
            w.id AS id,
            w.name AS name,
            w.code AS code,
            w.description AS description,
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

    @Select("""
        SELECT
            w.id AS id,
            w.name AS name,
            w.code AS code,
            w.description AS description,
            wm.role AS role,
            w.status AS status
        FROM workspace_members wm
        INNER JOIN workspaces w ON w.id = wm.workspace_id
        WHERE wm.user_id = #{userId}
          AND wm.workspace_id = #{workspaceId}
          AND wm.status = 'ACTIVE'
          AND w.status = 'ACTIVE'
        LIMIT 1
        """)
    WorkspaceDTO findActiveWorkspaceForUser(
            @Param("userId") Long userId,
            @Param("workspaceId") Long workspaceId
    );

    @Select("""
        SELECT
            p.id AS id,
            p.name AS name,
            p.description AS description,
            COALESCE(NULLIF(p.root_path, ''), p.code) AS language,
            p.file_count AS fileCount,
            to_char(p.updated_at, 'YYYY-MM-DD HH24:MI:SS') AS lastUpdated,
            p.status AS status
        FROM projects p
        INNER JOIN workspace_members wm ON wm.workspace_id = p.workspace_id
        WHERE p.workspace_id = #{workspaceId}
          AND wm.user_id = #{userId}
          AND wm.status = 'ACTIVE'
          AND COALESCE(p.status, 'ACTIVE') <> 'DELETED'
        ORDER BY p.updated_at DESC, p.id ASC
        """)
    List<ProjectDTO> findActiveProjectsForUser(
            @Param("userId") Long userId,
            @Param("workspaceId") Long workspaceId
    );
}
