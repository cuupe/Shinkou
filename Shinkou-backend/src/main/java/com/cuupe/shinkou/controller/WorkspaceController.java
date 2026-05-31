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

    @GetMapping("/my")
    public Result<List<WorkspaceDTO>> myWorkspaces(Authentication authentication) {
        return Result.success(workspaceService.my(authentication));
    }

    @PostMapping("/{workspaceId}/projects")
    public Result<ProjectFullDTO> createProjects(
            @PathVariable Long workspaceId,
            @Valid @RequestBody ProjectsRequest projectsRequest,
            Authentication authentication) {
        return Result.success(workspaceService.createProject(workspaceId, projectsRequest, authentication));
    }

    @GetMapping("/{workspaceId}/projects")
    public Result<List<ProjectFullDTO>> getProjects(
            @PathVariable Long workspaceId,
            Authentication authentication) {
        return Result.success(workspaceService.getProjects(workspaceId, authentication));
    }

    @GetMapping("/{workspaceId}/projects/{projectId}")
    public Result<ProjectFullDTO> getProject(
            @PathVariable(name = "workspaceId") Long workspaceId,
            @PathVariable(name = "projectId") Long projectId,
            Authentication authentication){
        return Result.success(workspaceService.getProject(workspaceId, projectId, authentication));
    }



}
