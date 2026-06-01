package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 当前用户信息数据传输对象，包含用户详情和工作区列表
 */
@Data
@Accessors(chain = true)
public class UserMe{
    private UserDTO user;
    private List<WorkspaceDTO> workspaces;
}
