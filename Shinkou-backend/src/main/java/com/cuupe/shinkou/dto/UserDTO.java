package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UserDTO{
    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private String department;
    private String position;
    private String status;
}
