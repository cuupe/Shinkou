package com.cuupe.shinkou.service.Impl;

import com.cuupe.shinkou.common.enums.ResultCode;
import com.cuupe.shinkou.common.exception.BusinessException;
import com.cuupe.shinkou.config.AuthProperties;
import com.cuupe.shinkou.dto.*;
import com.cuupe.shinkou.dto.request.ActivateRequest;
import com.cuupe.shinkou.dto.request.LoginRequest;
import com.cuupe.shinkou.dto.response.ActivateResponse;
import com.cuupe.shinkou.dto.response.LoginResponse;
import com.cuupe.shinkou.eneity.User;
import com.cuupe.shinkou.mapper.AuthMapper;
import com.cuupe.shinkou.mapper.InvitationMapper;
import com.cuupe.shinkou.security.JwtToken;
import com.cuupe.shinkou.security.JwtTokenProvider;
import com.cuupe.shinkou.service.AuthRedisService;
import com.cuupe.shinkou.service.AuthService;
import com.cuupe.shinkou.util.Data2DTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthMapper authMapper;
    private final InvitationMapper invitationMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRedisService authRedisService;
    private final AuthProperties authProperties;

    /**
     * 用户登录处理
     * 1. 验证邮箱格式和账号锁定状态
     * 2. 验证密码
     * 3. 检查账号状态
     * 4. 生成JWT令牌并保存到Redis
     * 5. 更新最后登录时间
     * @param request 登录请求
     * @return 登录响应，包含访问令牌和用户信息
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        // 检查账号是否被禁止
        checkLoginLocked(email);

        User user = authMapper.findUserByEmail(email);
        if (user == null || user.getPasswordHash() == null) {
            handleLoginFailed(email);
            throw new BusinessException(
                    ResultCode.AUTH_INVALID_CREDENTIALS.name(),
                    "企业邮箱或密码错误"
            );
        }

        // 匹配密码，明文与哈希值
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleLoginFailed(email);
            throw new BusinessException(
                    ResultCode.AUTH_INVALID_CREDENTIALS.name(),
                    "企业邮箱或密码错误"
            );
        }

        // 验证这个账号是否处于可用状态
        validateUserStatus(user);

        List<WorkspaceDTO> workspaces =
                authMapper.findActiveWorkspacesByUserId(user.getId());

        // 签发JWT
        JwtToken jwtToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail()
        );

        // Redis保存Token
        authRedisService.saveLoginToken(
                user.getId(),
                jwtToken.getTokenId(),
                jwtToken.getExpiresIn()
        );

        // 登录成功之后就清除过去的失败
        authRedisService.clearLoginFail(email);

        // 更新登录时间
        authMapper.updateLastLoginAt(user.getId());

        return new LoginResponse()
            .setAccessToken(jwtToken.getAccessToken())
            .setTokenType("Bearer")
            .setExpiresIn(jwtToken.getExpiresIn())
            .setUser(Data2DTO.user2UserDTO(user))
            .setWorkspaces(workspaces);
    }

    /**
     * 获取当前登录用户的详细信息
     * @param authentication Spring Security认证对象
     * @return 用户详细信息，包含工作区列表
     */
    @Transactional
    @Override
    public UserMe me(Authentication authentication) {
        Long id = currentUser(authentication);


        UserDTO user = authMapper.findUserById(id);
        if (user == null) {
            throw new BusinessException(
                    ResultCode.AUTH_UNAUTHORIZED.name(),
                    "用户不存在或登录状态已失效"
            );
        }

        List<WorkspaceDTO> workspaces = authMapper.findActiveWorkspacesByUserId(id);

        UserMe userMe = new UserMe();
        userMe.setUser(user);
        userMe.setWorkspaces(workspaces);

        return userMe;
    }


    /**
     * 激活用户账号（通过邀请链接）
     * 1. 验证邀请链接有效性
     * 2. 创建新用户或激活现有用户
     * 3. 将用户添加到工作区
     * 4. 标记邀请为已接受
     * 5. 生成JWT令牌
     * @param request 激活请求
     * @return 激活响应，包含访问令牌和已激活的工作区
     */
    @Transactional
    @Override
    public ActivateResponse activate(ActivateRequest request) {
        // 首先验证请求是否符合要求
        validateActivateRequest(request);

        // 取出token
        String invitationToken = request.getInvitationToken().trim();

        InvitationDTO invitation = invitationMapper.findTokenByToken(invitationToken);
        if(invitation == null){
            throw new BusinessException(
                    ResultCode.INVITATION_NOT_FOUND.name(),
                    "邀请链接不存在或已失效"
            );
        }

        // 验证邀请链接是否有效
        validateInvitation(invitation);

        // 获取需要的工作区
        ActivatedWorkspaceDTO workspace = authMapper.findWorkspaceById(
                invitation.getWorkspace().getId()
        );
        if(workspace == null){
            throw new BusinessException(
                    ResultCode.WORKSPACE_DISABLED.name(),
                    "工作区不存在或被禁用"
            );
        }

        String email = normalizeEmail(invitation.getEmail());
        User user = authMapper.findUserByEmail(email);

        // 如果用户不存在，就需要创建这个用户
        if(user == null){
            user = createUserFromInvitation(invitation,
                    request.getPassword());
        }
        // 如果存在，直接拉入
        else{
            user = activateExistingUser(
                    user,
                    invitation,
                    request.getPassword());
        }
        // 统计人数
        int memberCount = authMapper.countWorkspaceMember(
                invitation.getWorkspace().getId(),
                user.getId()
        );
        // 如果不存在这个用户，则添加
        if(memberCount == 0){
            authMapper.insertWorkspaceMember(
                    invitation.getWorkspace().getId(),
                    user.getId(),
                    invitation.getRole()
            );
        }

        int acceptedRows = authMapper.markInvitationAccepted(invitation.getId());
        if(acceptedRows == -1){
            throw new BusinessException(
                    ResultCode.INVITATION_ALREADY_ACCEPTED.name(),
                    "邀请链接已被使用"
            );
        }

        // 生成jwtToken
        JwtToken jwtToken = jwtTokenProvider.generateAccessToken(
                user.getId(),
                user.getEmail()
        );

        // 在Redis里保存jwtToken
        authRedisService.saveLoginToken(
                user.getId(),
                jwtToken.getTokenId(),
                jwtToken.getExpiresIn()
        );
        authMapper.updateLastLoginAt(user.getId());

        List<WorkspaceDTO> workspaces = authMapper.findActiveWorkspacesByUserId(user.getId());

        return new ActivateResponse()
                .setUser(Data2DTO.user2UserDTO(user))
                .setExpiresIn(jwtToken.getExpiresIn())
                .setActivatedWorkspace(workspace)
                .setWorkspaces(workspaces)
                .setTokenType("Bearer")
                .setAccessToken(jwtToken.getAccessToken());
    }

    /**
     * 用户登出处理
     * 1. 从请求中解析Token ID
     * 2. 从Redis中删除令牌
     * @param authentication Spring Security认证对象
     * @param request HTTP请求对象
     * @return 登出结果消息
     */
    @Override
    public String logout(Authentication authentication, HttpServletRequest request) {
        Long userId = currentUser(authentication);

        String tokenId = resolveToken(request);
        if(tokenId == null || tokenId.isBlank()){
            throw new BusinessException(
                    ResultCode.AUTH_TOKEN_INVALID.name(),
                    "Token 无效"
            );
        }

        authRedisService.logout(userId, tokenId);

        return "success";
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void checkLoginLocked(String email) {
        if (!authRedisService.isLoginLocked(email)) {
            return;
        }

        Long ttl = authRedisService.getLoginLockTtl(email);

        String message = ttl != null && ttl > 0
                ? "登录失败次数过多，请 " + ttl + " 秒后再试"
                : "登录失败次数过多，请稍后再试";

        throw new BusinessException(
                ResultCode.AUTH_ACCOUNT_LOCKED.name(),
                message
        );
    }

    private void handleLoginFailed(String email) {
        long failCount = authRedisService.increaseLoginFailCount(email);

        if (failCount >= authProperties.getLoginFailMaxCount()) {
            authRedisService.lockLogin(email);

            throw new BusinessException(
                    ResultCode.AUTH_ACCOUNT_LOCKED.name(),
                    "登录失败次数过多，请稍后再试"
            );
        }
    }

    /**
     * 验证用户状态
     * @param user 用户实体对象
     */
    private void validateUserStatus(User user) {
        String status = user.getStatus();

        if ("PENDING".equals(status)) {
            throw new BusinessException(
                    ResultCode.AUTH_ACCOUNT_NOT_ACTIVATED.name(),
                    "账号尚未激活，请通过邀请链接完成账号激活"
            );
        }

        if ("DISABLED".equals(status)) {
            throw new BusinessException(
                    ResultCode.AUTH_ACCOUNT_DISABLED.name(),
                    "账号已被禁用"
            );
        }

        if ("LOCKED".equals(status)) {
            throw new BusinessException(
                    ResultCode.AUTH_ACCOUNT_LOCKED.name(),
                    "账号已锁定，请稍后再试或联系管理员"
            );
        }

        if (!"ACTIVE".equals(status)) {
            throw new BusinessException(
                    ResultCode.AUTH_INVALID_CREDENTIALS.name(),
                    "企业邮箱或密码错误"
            );
        }
    }

    /**
     * 激活存在的用户到工作区
     * @param user 用户实体对象
     * @param invitation 邀请 DTO 对象
     * @param rawPassword 原始密码
     * @return 用户实体对象
     */
    private User activateExistingUser(User user, InvitationDTO invitation, String rawPassword) {
        if ("DISABLED".equals(user.getStatus())) {
            throw new BusinessException(
                    ResultCode.AUTH_ACCOUNT_DISABLED.name(),
                    "账号已被禁用"
            );
        }

        if ("LOCKED".equals(user.getStatus())) {
            throw new BusinessException(
                    ResultCode.AUTH_ACCOUNT_LOCKED.name(),
                    "账号已锁定，请稍后再试或联系管理员"
            );
        }

        // 已经激活的用户：说明可能是被邀请加入新的 workspace
        // 这种情况下不要覆盖密码
        if ("ACTIVE".equals(user.getStatus())) {
            return user;
        }

        user.setPasswordHash(passwordEncoder.encode(rawPassword))
            .setName(invitation.getName())
            .setDepartment(invitation.getDepartment())
            .setPosition(invitation.getPosition())
            .setStatus("ACTIVE");

        authMapper.activateUser(user);

        return user;
    }

    /**
     * 通过邀请创建用户
     * @param invitation 邀请 DTO 对象
     * @param rawPassword 原始密码
     * @return 创建的用户实体对象
     */
    private User createUserFromInvitation(InvitationDTO invitation, String rawPassword) {
        User user = new User()
            .setEmail(normalizeEmail(invitation.getEmail()))
            .setPasswordHash(passwordEncoder.encode(rawPassword))
            .setName(invitation.getName())
            .setDepartment(invitation.getDepartment())
            .setPosition(invitation.getPosition())
            .setStatus("ACTIVE");

        authMapper.insertUser(user);

        return user;
    }

    /**
     * 验证邀请是否有效
     * @param invitation 邀请 DTO 实体
     */
    private void validateInvitation(InvitationDTO invitation) {
        String status = invitation.getStatus();

        if ("REVOKED".equals(status)) {
            throw new BusinessException(
                    ResultCode.INVITATION_REVOKED.name(),
                    "邀请链接已撤销，请联系管理员重新发送"
            );
        }

        if ("ACCEPTED".equals(status)) {
            throw new BusinessException(
                    ResultCode.INVITATION_ALREADY_ACCEPTED.name(),
                    "邀请链接已被使用"
            );
        }

        if (invitation.getExpiresAt() != null
                && invitation.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException(
                    ResultCode.INVITATION_EXPIRED.name(),
                    "邀请链接已过期，请联系管理员重新发送"
            );
        }

        if (!"PENDING".equals(status)) {
            throw new BusinessException(
                    ResultCode.INVITATION_NOT_FOUND.name(),
                    "邀请状态异常"
            );
        }
    }

    /**
     * 严格的激活请求验证
     * @param request 激活请求实体
     */
    private void validateActivateRequest(ActivateRequest request) {
        if (request == null) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "请求参数不能为空"
            );
        }

        if (request.getInvitationToken() == null || request.getInvitationToken().isBlank()) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "邀请 token 不能为空"
            );
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "密码不能为空"
            );
        }

        if (request.getConfirmPassword() == null || request.getConfirmPassword().isBlank()) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "确认密码不能为空"
            );
        }

        String password = request.getPassword();

        if (!password.equals(request.getConfirmPassword())) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "两次输入的密码不一致"
            );
        }

        if (password.length() < 8 || password.length() > 32) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "密码长度必须为 8-32 位"
            );
        }

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*].*");

        if (!hasUppercase || !hasLowercase || !hasNumber || !hasSpecial) {
            throw new BusinessException(
                    ResultCode.VALIDATION_ERROR.name(),
                    "密码必须包含大小写字母、数字和特殊字符（如 !@#$%^&*）"
            );
        }
    }

    /**
     * 获取当前用户 id，兼有验证用户的作用
     * @param authentication 验证体
     * @return 用户 ID
     */
    private @NonNull Long currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(
                    ResultCode.AUTH_UNAUTHORIZED.name(),
                    "未登录或没有认证信息"
            );
        }

        Object principal = authentication.getPrincipal();
        if(!(principal instanceof Long id)){
            throw new BusinessException(
                    ResultCode.AUTH_TOKEN_INVALID.name(),
                    "Token 无效"
            );
        }
        return id;
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length());
        }

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("access_token".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

}
