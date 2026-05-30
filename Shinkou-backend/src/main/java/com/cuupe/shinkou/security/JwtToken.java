package com.cuupe.shinkou.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtToken {

    /**
     * 返回给前端的 accessToken。
     */
    private String accessToken;

    /**
     * JWT 的 jti。
     * 用于 Redis Key: auth:token:{userId}:{tokenId}
     */
    private String tokenId;

    /**
     * token 有效期，单位秒。
     */
    private Long expiresIn;
}
