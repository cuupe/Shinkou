package com.cuupe.shinkou.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * JWT 签名密钥。
     * 生产环境不要写死在 yml，建议用环境变量。
     * @return 签名密钥字符串
     */
    private String secret;

    /**
     * accessToken 有效期，单位秒。
     * @return 过期时间（秒）
     */
    private Long expiresIn = 7200L;
}
