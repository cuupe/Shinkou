package com.cuupe.shinkou.service;

import com.cuupe.shinkou.dto.ProjectFullDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.dto.request.ProjectsRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WorkspaceService {
    /**
     * 获取当前用户的所有工作区列表
     * @param authentication Spring Security认证对象
     * @return 工作区DTO列表
     */
    List<WorkspaceDTO> my(Authentication authentication);

    /**
     * 在指定工作区创建新项目
     * @param workspaceId 工作区ID
     * @param projectsRequest 项目创建请求
     * @param authentication Spring Security认证对象
     * @return 创建的项目完整信息DTO
     */
    ProjectFullDTO createProject(Long workspaceId, ProjectsRequest projectsRequest, Authentication authentication);

    /**
     * 获取指定工作区下的所有项目列表
     * @param workspaceId 工作区ID
     * @param authentication Spring Security认证对象
     * @return 项目完整信息DTO列表
     */
    List<ProjectFullDTO> getProjects(Long workspaceId, Authentication authentication);

    /**
     * 获取指定工作区下的特定项目详情
     * @param workspaceId 工作区ID
     * @param projectId 项目ID
     * @param authentication Spring Security认证对象
     * @return 项目完整信息DTO
     */
    ProjectFullDTO getProject(Long workspaceId, Long projectId, Authentication authentication);
}
