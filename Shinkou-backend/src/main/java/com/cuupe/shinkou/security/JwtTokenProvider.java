package com.cuupe.shinkou.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;

    /**
     * 生成JWT访问令牌
     * @param userId 用户ID
     * @param email 用户邮箱
     * @return JWT令牌对象，包含accessToken、tokenId和过期时间
     */
    public JwtToken generateAccessToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtProperties.getExpiresIn());

        String tokenId = UUID.randomUUID().toString();

        String accessToken = Jwts.builder()
                .id(tokenId)
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSecretKey())
                .compact();

        return new JwtToken(
                accessToken,
                tokenId,
                jwtProperties.getExpiresIn()
        );
    }

    /**
     * 解析JWT令牌并获取Claims信息
     * @param accessToken JWT访问令牌
     * @return Claims对象，包含令牌中的所有声明
     */
    public Claims parseClaims(String accessToken) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    /**
     * 获取签名密钥
     * @return HMAC SHA密钥对象
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }
}
