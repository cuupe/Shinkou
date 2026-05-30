package com.cuupe.shinkou.service;

import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.dto.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public LoginResponse login(LoginRequest request);

    UserMe me(Authentication authentication);

    ActivateResponse activate(@Valid ActivateRequest request);
}
