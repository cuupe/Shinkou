package com.cuupe.shinkou.service.Impl;

import com.cuupe.shinkou.common.enums.ResultCode;
import com.cuupe.shinkou.common.exception.BusinessException;
import com.cuupe.shinkou.dto.ProjectFullDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.dto.request.ProjectsRequest;
import com.cuupe.shinkou.eneity.Project;
import com.cuupe.shinkou.mapper.WorkspaceMapper;
import com.cuupe.shinkou.service.WorkspaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceMapper workspaceMapper;


    /**
     * 获取当前用户的所有工作区列表
     * @param authentication Spring Security认证对象
     * @return 工作区DTO列表
     */
    @Override
    public List<WorkspaceDTO> my(Authentication authentication) {
        Long id = currentUserId(authentication);
        if(id == null || id < 0L){
            throw new BusinessException(
                    ResultCode.FAILURE.name(),
                    "不允许的ID"
            );
        }

        return workspaceMapper.findActiveWorkspacesByUserId(id);
    }

    /**
     * 在指定工作区创建新项目
     * 1. 验证用户权限
     * 2. 验证项目请求参数
     * 3. 检查项目code是否重复
     * 4. 创建项目并返回完整信息
     * @param workspaceId 工作区ID
     * @param projectsRequest 项目创建请求
     * @param authentication Spring Security认证对象
     * @return 创建的项目完整信息
     */
    @Override
    public ProjectFullDTO createProject(Long workspaceId,
                                        ProjectsRequest projectsRequest,
                                        Authentication authentication) {
        // 基础能力校验
        Long userId = currentUserId(authentication);
        checkUserAccess(workspaceId, userId);
        validateCreateProjectRequest(projectsRequest);

        String code = projectsRequest.getCode().trim().toLowerCase();

        if (workspaceMapper.countProjectByWorkspaceIdAndCode(workspaceId, code) > 0) {
            throw new BusinessException(
                    ResultCode.PROJECT_ALREADY_EXISTS.name(),
                    "该工作区下已存在相同 code 的项目"
            );
        }

        ProjectFullDTO project = workspaceMapper.createProject(new Project()
                .setCode(projectsRequest.getCode())
                .setWorkspaceId(workspaceId)
                .setDescription(projectsRequest.getDescription())
                .setName(projectsRequest.getName())
                .setCreatedBy(userId)
                .setFileCount(0)
                .setRootPath(null));
        if(project == null){
            throw new BusinessException(
                    ResultCode.FAILURE.name(),
                    "新建项目失败，请联系管理员查明原因"
            );
        }

        return project;
    }

    /**
     * 获取指定工作区下的所有项目列表
     * @param workspaceId 工作区ID
     * @param authentication Spring Security认证对象
     * @return 项目完整信息DTO列表
     */
    @Override
    public List<ProjectFullDTO> getProjects(Long workspaceId,
                                            Authentication authentication) {
        Long userId = currentUserId(authentication);

        checkUserAccess(workspaceId, userId);

        // List 形式一般不需要考虑校验，空的话直接返回空了
        return workspaceMapper.getProjectsByWorkspaceId(workspaceId);
    }

    /**
     * 获取指定工作区下的特定项目详情
     * @param workspaceId 工作区ID
     * @param projectId 项目ID
     * @param authentication Spring Security认证对象
     * @return 项目完整信息DTO
     */
    @Override
    public ProjectFullDTO getProject(Long workspaceId,
                                     Long projectId,
                                     Authentication authentication) {
        Long userId = currentUserId(authentication);

        // 校验当前用户是否属于这个 workspace
        checkUserAccess(workspaceId, userId);

        ProjectFullDTO projectByWorkspaceIdAndProjectId =
                workspaceMapper.getProjectByWorkspaceIdAndProjectId(workspaceId, projectId);
        if(projectByWorkspaceIdAndProjectId == null){
            throw new BusinessException(
                    ResultCode.PROJECT_NOT_FOUND.name(),
                    "未找到相关项目，请确保工作区id和项目id正确"
            );
        }

        return projectByWorkspaceIdAndProjectId;
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new BusinessException(
                    ResultCode.AUTH_UNAUTHORIZED.name(),
                    "未登录或登录已过期");
        }
        return userId;
    }

    private void checkUserAccess(Long workspaceId, Long userId) {
        if (workspaceMapper.existsActiveMember(workspaceId, userId) <= 0) {
            throw new BusinessException(
                    ResultCode.WORKSPACE_ACCESS_DENIED.name(),
                    "你没有该工作区的访问权限"
            );
        }
    }

    private void checkCreatePermission(Long workspaceId, Long userId){
        String role = workspaceMapper.findActiveMemberRole(workspaceId, userId);

    }

    private void validateCreateProjectRequest(ProjectsRequest request) {
        if (request == null) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "请求参数不能为空"
            );
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "项目名称不能为空"
            );
        }

        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "项目 code 不能为空"
            );
        }

        if (!request.getCode().matches("^[a-z0-9][a-z0-9-]{1,98}[a-z0-9]$")) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "项目 code 只能包含小写字母、数字和短横线，且不能以短横线开头或结尾"
            );
        }
    }
}
