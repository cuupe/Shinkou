package com.cuupe.shinkou.controller;

import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.dto.InvitationDTO;
import com.cuupe.shinkou.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {
    private final InvitationService invitationService;

    @GetMapping("/{token}")
    public Result<InvitationDTO> tokenCheck(@PathVariable String token){
        return Result.success(invitationService.tokenCheck(token));
    }

}
