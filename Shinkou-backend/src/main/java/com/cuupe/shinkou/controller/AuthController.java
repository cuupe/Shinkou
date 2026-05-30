package com.cuupe.shinkou.controller;

import com.cuupe.shinkou.dto.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.cuupe.shinkou.common.response.Result;
import com.cuupe.shinkou.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    public Result<UserMe> me(Authentication authentication){
        return Result.success(authService.me(authentication));
    }


    @PostMapping("/activate")
    public Result<ActivateResponse> activate(@Valid @RequestBody ActivateRequest request){
        return Result.success(authService.activate(request));
    }

    @PostMapping("/logout")
    public Result<?> logout(Long id){
        return null;
    }
}
