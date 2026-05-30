package com.cuupe.shinkou.config;

import com.cuupe.shinkou.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AuthProperties.class,
        JwtProperties.class
})
public class AuthConfig {

}
