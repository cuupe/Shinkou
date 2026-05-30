package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class WorkspaceDTO{
    private Long id;
    private String name;
    private String code;
    private String description;
    private String role;
    private String status;
}
