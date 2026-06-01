package com.cuupe.shinkou.config;

import com.cuupe.shinkou.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 认证配置类
 * 启用AuthProperties和JwtProperties配置属性
 */
@Configuration
@EnableConfigurationProperties({
        AuthProperties.class,
        JwtProperties.class
})
public class AuthConfig {

}
