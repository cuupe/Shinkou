package com.cuupe.shinkou.service;

import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.dto.InvitationDTO;
import org.springframework.stereotype.Service;

@Service
public interface InvitationService {

    /**
     * 验证邀请令牌的有效性
     * @param token 邀请令牌字符串
     * @return 邀请信息DTO，包含工作区和用户信息
     */
    InvitationDTO tokenCheck(String token);
}
