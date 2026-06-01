package com.cuupe.shinkou.controller;

import com.cuupe.shinkou.dto.*;
import com.cuupe.shinkou.dto.request.ActivateRequest;
import com.cuupe.shinkou.dto.request.LoginRequest;
import com.cuupe.shinkou.dto.response.ActivateResponse;
import com.cuupe.shinkou.dto.response.LoginResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
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

    /**
     * 用户登录接口
     * @param request 登录请求，包含邮箱和密码
     * @param response HTTP响应对象，用于设置Cookie
     * @return 登录响应，包含用户信息和工作区列表
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response){
        LoginResponse loginResponse = authService.login(request);

        addAccessTokenCookie(
                response,
                loginResponse.getAccessToken(),
                loginResponse.getExpiresIn()
        );

        loginResponse.setAccessToken(null)
                .setTokenType(null);

        return Result.success(loginResponse);
    }

    /**
     * 获取当前登录用户信息
     * @param authentication Spring Security认证对象
     * @return 当前用户的详细信息
     */
    @GetMapping("/me")
    public Result<UserMe> me(Authentication authentication){
        return Result.success(authService.me(authentication));
    }


    /**
     * 激活用户账号（通过邀请链接）
     * @param request 激活请求，包含邀请token和密码
     * @param response HTTP响应对象，用于设置Cookie
     * @return 激活响应，包含用户信息和已激活的工作区
     */
    @PostMapping("/activate")
    public Result<ActivateResponse> activate(
            @Valid @RequestBody ActivateRequest request,
            HttpServletResponse response){
        ActivateResponse activateResponse = authService.activate(request);

        addAccessTokenCookie(
                response,
                activateResponse.getAccessToken(),
                activateResponse.getExpiresIn()
        );

        activateResponse.setAccessToken(null)
                .setTokenType(null);


        return Result.success(activateResponse);
    }

    /**
     * 用户登出接口
     * @param authentication Spring Security认证对象
     * @param request HTTP请求对象，用于获取Token
     * @param response HTTP响应对象，用于清除Cookie
     * @return 登出结果消息
     */
    @PostMapping("/logout")
    public Result<String> logout(Authentication authentication,
                                 HttpServletRequest request,
                                 HttpServletResponse response){
        String result = authService.logout(authentication, request);
        clearAccessTokenCookie(response);
        return Result.success(result);
    }

    /**
     * 添加访问令牌到HTTP Cookie
     * @param response HTTP响应对象
     * @param accessToken JWT访问令牌
     * @param expiresIn 令牌过期时间（秒）
     */
    private void addAccessTokenCookie(
            @NonNull HttpServletResponse response,
            String accessToken,
            @NonNull Long expiresIn) {
        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setHttpOnly(true);
        // 上线后 HTTPS 改 true
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(expiresIn.intValue());

        response.addCookie(cookie);
    }

    /**
     * 清除访问令牌Cookie
     * @param response HTTP响应对象
     */
    private void clearAccessTokenCookie(@NonNull HttpServletResponse response){
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}
