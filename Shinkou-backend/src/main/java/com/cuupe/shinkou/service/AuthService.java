package com.cuupe.shinkou.service;

import com.cuupe.shinkou.dto.*;
import com.cuupe.shinkou.dto.request.ActivateRequest;
import com.cuupe.shinkou.dto.request.LoginRequest;
import com.cuupe.shinkou.dto.response.ActivateResponse;
import com.cuupe.shinkou.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    /**
     * 用户登录
     * @param request 登录请求，包含邮箱和密码
     * @return 登录响应，包含访问令牌和用户信息
     */
    public LoginResponse login(LoginRequest request);

    /**
     * 获取当前登录用户的详细信息
     * @param authentication Spring Security认证对象
     * @return 用户详细信息，包含工作区列表
     */
    UserMe me(Authentication authentication);

    /**
     * 激活用户账号（通过邀请链接）
     * @param request 激活请求，包含邀请token和密码
     * @return 激活响应，包含访问令牌和已激活的工作区信息
     */
    ActivateResponse activate(@Valid ActivateRequest request);

    /**
     * 用户登出
     * @param authentication Spring Security认证对象
     * @param request HTTP请求对象
     * @return 登出结果消息
     */
    String logout(Authentication authentication,
                  HttpServletRequest request);
}
