package com.cuupe.shinkou.mapper;

import com.cuupe.shinkou.dto.ProjectFullDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.dto.request.ProjectsRequest;
import com.cuupe.shinkou.eneity.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WorkspaceMapper {

    /**
     * 查询用户的所有活跃工作区
     * @param userId 用户ID
     * @return 工作区DTO列表
     */
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


    /**
     * 根据工作区ID和项目ID查询项目
     * @param workspaceId 工作区ID
     * @param projectId 项目ID
     * @return 项目完整信息DTO
     */
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

    /**
     * 根据工作区ID查询所有项目
     * @param workspaceId 工作区ID
     * @return 项目完整信息DTO列表
     */
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

    /**
     * 检查用户是否是工作区的活跃成员
     * @param workspaceId 工作区ID
     * @param userId 用户ID
     * @return 存在的记录数
     */
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


    /**
     * 创建新项目
     * @param project 项目实体对象
     * @return 创建的项目完整信息DTO
     */
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


    /**
     * 查询工作区成员的活跃角色
     * @param workspaceId 工作区ID
     * @param userId 用户ID
     * @return 角色字符串
     */
    @Select("""
        SELECT
            
        
        """)
    String findActiveMemberRole(Long workspaceId, Long userId);


    /**
     * 统计工作区下指定code的项目数量
     * @param workspaceId 工作区ID
     * @param code 项目code
     * @return 项目数量
     */
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
