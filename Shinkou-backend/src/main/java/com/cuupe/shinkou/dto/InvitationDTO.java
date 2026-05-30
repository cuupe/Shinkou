package com.cuupe.shinkou.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class InvitationDTO{
    private Long id;
    private String token;
    private String email;
    private String name;
    private String department;
    private String position;
    private String role;
    private String status;
    private LocalDateTime expiresAt;
    private ActivatedWorkspaceDTO workspace;
}
