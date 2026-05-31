package com.cuupe.shinkou.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProjectsRequest {
    @NotBlank(message = "项目名不得为空")
    private String name;

    @NotBlank(message = "code不得为空")
    private String code;
    private String description;
}
