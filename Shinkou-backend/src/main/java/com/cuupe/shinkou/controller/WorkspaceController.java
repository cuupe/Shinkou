package com.cuupe.shinkou.controller;

import com.cuupe.shinkou.common.enums.ResultCode;
import com.cuupe.shinkou.common.exception.BusinessException;
import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.dto.ProjectDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.mapper.WorkspaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceMapper workspaceMapper;

    @GetMapping("/my")
    public Result<List<WorkspaceDTO>> myWorkspaces(Authentication authentication) {
        return Result.success(
                workspaceMapper.findActiveWorkspacesByUserId(currentUserId(authentication))
        );
    }

    @GetMapping("/{workspaceId}")
    public Result<WorkspaceDTO> workspace(
            @PathVariable Long workspaceId,
            Authentication authentication
    ) {
        WorkspaceDTO workspace = workspaceMapper.findActiveWorkspaceForUser(
                currentUserId(authentication),
                workspaceId
        );

        if (workspace == null) {
            throw new BusinessException(
                    ResultCode.WORKSPACE_ACCESS_DENIED.name(),
                    "无权访问该工作区");
        }

        return Result.success(workspace);
    }

    @GetMapping("/{workspaceId}/projects")
    public Result<List<ProjectDTO>> projects(
            @PathVariable Long workspaceId,
            Authentication authentication
    ) {
        Long userId = currentUserId(authentication);

        if (workspaceMapper.findActiveWorkspaceForUser(userId, workspaceId) == null) {
            throw new BusinessException(
                    ResultCode.WORKSPACE_ACCESS_DENIED.name(),
                    "无权访问该工作区");
        }

        return Result.success(
                workspaceMapper.findActiveProjectsForUser(userId, workspaceId)
        );
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new BusinessException(
                    ResultCode.AUTH_UNAUTHORIZED.name(),
                    "未登录或登录已过期");
        }
        return userId;
    }
}
