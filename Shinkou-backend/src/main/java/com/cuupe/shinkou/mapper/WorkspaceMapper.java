package com.cuupe.shinkou.mapper;

import com.cuupe.shinkou.dto.ProjectFullDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.dto.request.ProjectsRequest;
import com.cuupe.shinkou.eneity.Project;
import org.apache.ibatis.annotations.*;

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
            id,
            workspace_id AS workspaceId,
            name,
            code,
            description,
            root_path AS rootPath,
            file_count AS fileCount,
            status,
            created_by AS createdBy,
            created_at AS createdAt,
            updated_at AS updatedAt
            FROM projects
            WHERE id = #{projectId}
            AND workpace_id = #{workspaceId}
            AND status = 'ACTIVE'
        """)
    ProjectFullDTO getProjectByWorkspaceIdAndProjectId(@Param("workspaceId") Long workspaceId,
                                                       @Param("projectId") Long projectId);

    @Select("""
         SELECT
            id,
            workspace_id AS workspaceId,
            name,
            code,
            description,
            root_path AS rootPath,
            file_count AS fileCount,
            status,
            created_by AS createdBy,
            created_at AS createdAt,
            updated_at AS updatedAt
            FROM projects
            WHERE workpace_id = #{workspaceId}
            AND status = 'ACTIVE'
        """)
    List<ProjectFullDTO> getProjectsByWorkspaceId(@Param("workspaceId") Long workspaceId);

    @Select("""
            SELECT COUNT(1)
            FROM workspace_members wm
            JOIN workspaces w ON w.id = wm.workspace_id
            WHERE wm.workspace_id = #{workspaceId}
              AND wm.user_id = #{userId}
              AND wm.status = 'ACTIVE'
              AND w.status = 'ACTIVE'
        """)
    int existsActiveMember(@Param("workspaceId") Long workspaceId,
                           @Param("userId") Long userId);


    @Insert("""
            INSERT INTO projects (
                workspace_id,
                name,
                code,
                description,
                root_path,
                file_count,
                status,
                created_by,
                created_at,
                updated_at
            ) VALUES (
                #{workspaceId},
                #{name},
                #{code},
                #{description},
                #{rootPath},
                #{fileCount},
                #{status},
                #{createdBy},
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    ProjectFullDTO createProject(Project project);


    @Select("""
        SELECT
            
        
        """)
    String findActiveMemberRole(Long workspaceId, Long userId);


    @Select("""
            SELECT
                COUNT(1)
            FROM projects
            WHERE workspace_id = #{workspaceId}
            AND code = #{code}
            """)
    int countProjectByWorkspaceIdAndCode(@Param("workspaceId") Long workspaceId,
                                         @Param("code") String code);
}
