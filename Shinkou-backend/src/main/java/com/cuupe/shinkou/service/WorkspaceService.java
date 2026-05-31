package com.cuupe.shinkou.service;

import com.cuupe.shinkou.dto.ProjectFullDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.dto.request.ProjectsRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WorkspaceService {
    List<WorkspaceDTO> my(Authentication authentication);

    ProjectFullDTO createProject(Long workspaceId, ProjectsRequest projectsRequest, Authentication authentication);

    List<ProjectFullDTO> getProjects(Long workspaceId, Authentication authentication);

    ProjectFullDTO getProject(Long workspaceId, Long projectId, Authentication authentication);
}
