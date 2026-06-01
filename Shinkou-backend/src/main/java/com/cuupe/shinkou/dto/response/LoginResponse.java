package com.cuupe.shinkou.dto.response;

import com.cuupe.shinkou.dto.UserDTO;
import com.cuupe.shinkou.dto.WorkspaceDTO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 登录响应数据传输对象
 */
@Data
@Accessors(chain = true)
public class LoginResponse{
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private UserDTO user;
    private List<WorkspaceDTO> workspaces;
}
