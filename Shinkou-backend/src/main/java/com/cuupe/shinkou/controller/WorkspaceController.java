package com.cuupe.shinkou.controller;

import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.dto.ProjectFullDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.dto.request.ProjectsRequest;
import com.cuupe.shinkou.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    /**
     * 获取当前用户的所有工作区列表
     * @param authentication Spring Security认证对象
     * @return 工作区 DTO 列表
     */
    @GetMapping("/my")
    public Result<List<WorkspaceDTO>> myWorkspaces(Authentication authentication) {
        return Result.success(workspaceService.my(authentication));
    }

    /**
     * 在工作区中创建新项目
     * @param workspaceId 工作区 ID
     * @param projectsRequest 项目创建请求
     * @param authentication Spring Security 认证对象
     * @return 创建的项目完整信息
     */
    @PostMapping("/{workspaceId}/projects")
    public Result<ProjectFullDTO> createProjects(
            @PathVariable Long workspaceId,
            @Valid @RequestBody ProjectsRequest projectsRequest,
            Authentication authentication) {
        return Result.success(workspaceService.createProject(workspaceId, projectsRequest, authentication));
    }

    /**
     * 获取指定工作区下的所有项目列表
     * @param workspaceId 工作区 ID
     * @param authentication Spring Security认证对象
     * @return 项目完整信息 DTO 列表
     */
    @GetMapping("/{workspaceId}/projects")
    public Result<List<ProjectFullDTO>> getProjects(
            @PathVariable Long workspaceId,
            Authentication authentication) {
        return Result.success(workspaceService.getProjects(workspaceId, authentication));
    }

    /**
     * 获取指定工作区下的特定项目详情
     * @param workspaceId 工作区 ID
     * @param projectId 项目 ID
     * @param authentication Spring Security认证对象
     * @return 项目完整信息 DTO
     */
    @GetMapping("/{workspaceId}/projects/{projectId}")
    public Result<ProjectFullDTO> getProject(
            @PathVariable(name = "workspaceId") Long workspaceId,
            @PathVariable(name = "projectId") Long projectId,
            Authentication authentication){
        return Result.success(workspaceService.getProject(workspaceId, projectId, authentication));
    }



}
