package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String language;
    private Integer fileCount;
    private String lastUpdated;
    private String status;
}
