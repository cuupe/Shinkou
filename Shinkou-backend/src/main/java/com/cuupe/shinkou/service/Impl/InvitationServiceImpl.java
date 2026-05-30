package com.cuupe.shinkou.service.Impl;

import com.cuupe.shinkou.common.enums.ResultCode;
import com.cuupe.shinkou.common.exception.BusinessException;
import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.dto.InvitationDTO;
import com.cuupe.shinkou.mapper.InvitationMapper;
import com.cuupe.shinkou.service.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {

    private final InvitationMapper invitationMapper;

    @Override
    public InvitationDTO tokenCheck(String token) {
        if(token == null || token.isBlank()){
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "邀请码不能为空"
            );
        }

        InvitationDTO invitation = invitationMapper.findTokenByToken(token);
        tokenValidation(invitation);

        return invitation;
    }


    private void tokenValidation(InvitationDTO invitation) {
        if(invitation == null){
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "邀请链接不存在或失效"
            );
        }

        if ("REVOKED".equals(invitation.getStatus())) {
            throw new BusinessException(
                    ResultCode.INVITATION_REVOKED.name(),
                    "邀请链接已撤销，请联系管理员重新发送"
            );
        }

        if ("ACCEPTED".equals(invitation.getStatus())) {
            throw new BusinessException(
                    ResultCode.INVITATION_ALREADY_ACCEPTED.name(),
                    "邀请链接已被使用"
            );
        }

        if (invitation.getExpiresAt() != null
                && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(
                    ResultCode.INVITATION_EXPIRED.name(),
                    "邀请链接已过期，请联系管理员重新发送"
            );
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new BusinessException(
                    ResultCode.INVITATION_NOT_FOUND.name(),
                    "邀请状态异常"
            );
        }
    }
}
