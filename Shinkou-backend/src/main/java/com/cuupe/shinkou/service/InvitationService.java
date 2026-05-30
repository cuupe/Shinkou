package com.cuupe.shinkou.service;

import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.dto.InvitationDTO;
import org.springframework.stereotype.Service;

@Service
public interface InvitationService {

    InvitationDTO tokenCheck(String token);
}
