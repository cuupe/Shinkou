package com.cuupe.shinkou.service;

import com.cuupe.shinkou.dto.*;
import com.cuupe.shinkou.dto.request.ActivateRequest;
import com.cuupe.shinkou.dto.request.LoginRequest;
import com.cuupe.shinkou.dto.response.ActivateResponse;
import com.cuupe.shinkou.dto.response.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    public LoginResponse login(LoginRequest request);

    UserMe me(Authentication authentication);

    ActivateResponse activate(@Valid ActivateRequest request);
}
