package com.cuupe.shinkou.security;

import com.cuupe.shinkou.service.AuthRedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthRedisService authRedisService;

    /**
     * 过滤器核心方法，处理每个请求的JWT认证
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String accessToken = resolveToken(request);

        if (accessToken != null
                && !accessToken.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(accessToken, request);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 验证JWT令牌并设置Spring Security认证信息
     * @param accessToken JWT访问令牌
     * @param request HTTP请求对象
     */
    private void authenticate(String accessToken, HttpServletRequest request) {
        try {
            Claims claims = jwtTokenProvider.parseClaims(accessToken);
            Long userId = Long.valueOf(claims.getSubject());
            String tokenId = claims.getId();

            if (tokenId == null || tokenId.isBlank()
                    || !authRedisService.hasLoginToken(userId, tokenId)) {
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, List.of());
            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException | IllegalArgumentException ignored) {
            SecurityContextHolder.clearContext();
        }
    }

    /**
     * 从HTTP请求中解析JWT令牌
     * 优先从Authorization header获取，其次从Cookie获取
     * @param request HTTP请求对象
     * @return JWT令牌字符串，未找到则返回null
     */
    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
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
