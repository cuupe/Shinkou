package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ActivatedWorkspaceDTO {
    private Long id;
    private String name;
    private String code;
}
