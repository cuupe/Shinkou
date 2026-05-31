package com.cuupe.shinkou.util;

import com.cuupe.shinkou.dto.ProjectFullDTO;
import com.cuupe.shinkou.dto.UserDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import com.cuupe.shinkou.eneity.Project;
import com.cuupe.shinkou.eneity.User;
import com.cuupe.shinkou.eneity.Workspace;

public class Data2DTO {
    public static UserDTO user2UserDTO(User user){
        return new UserDTO()
                .setId(user.getId())
                .setName(user.getName())
                .setEmail(user.getEmail())
                .setPosition(user.getPosition())
                .setDepartment(user.getDepartment())
                .setStatus(user.getStatus())
                .setAvatarUrl(user.getAvatarUrl());
    }

    public static ProjectFullDTO project2ProjectFullDTO(Project project) {
        return new ProjectFullDTO()
                .setCode(project.getCode())
                .setId(project.getId())
                .setDescription(project.getDescription())
                .setCreatedBy(project.getCreatedBy())
                .setFileCount(project.getFileCount())
                .setCreatedAt(project.getCreatedAt())
                .setRootPath(project.getRootPath())
                .setUpdatedAt(project.getUpdatedAt())
                .setWorkspaceId(project.getWorkspaceId())
                .setName(project.getName())
                .setStatus(project.getStatus());
    }

}
