package com.cuupe.shinkou.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateRequest {
    @NotBlank(message = "邀请码不得为空")
    private String invitationToken;

    @NotBlank(message = "密码不得为空")
    private String password;

    @NotBlank(message = "不允许空的确认密码")
    private String confirmPassword;
}
