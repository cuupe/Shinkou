package com.cuupe.shinkou.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 项目创建请求数据传输对象
 */
@Data
public class ProjectsRequest {
    @NotBlank(message = "项目名不得为空")
    private String name;

    @NotBlank(message = "code不得为空")
    private String code;
    private String description;
}
