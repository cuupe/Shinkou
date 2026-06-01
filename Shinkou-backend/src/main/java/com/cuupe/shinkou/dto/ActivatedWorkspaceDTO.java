package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 已激活工作区数据传输对象
 */
@Data
@Accessors(chain = true)
public class ActivatedWorkspaceDTO {
    private Long id;
    private String name;
    private String code;
}
