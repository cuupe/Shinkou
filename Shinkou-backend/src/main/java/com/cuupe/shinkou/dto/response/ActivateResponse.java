package com.cuupe.shinkou.dto.response;

import com.cuupe.shinkou.dto.ActivatedWorkspaceDTO;
import com.cuupe.shinkou.dto.UserDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 激活响应数据传输对象
 */
@Data
@Accessors(chain = true)
public class ActivateResponse{
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserDTO user;
    private List<WorkspaceDTO> workspaces;
    private ActivatedWorkspaceDTO activatedWorkspace;
}
