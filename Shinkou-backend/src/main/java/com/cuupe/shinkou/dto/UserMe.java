package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class UserMe{
    private UserDTO user;
    private List<WorkspaceDTO> workspaces;
}
