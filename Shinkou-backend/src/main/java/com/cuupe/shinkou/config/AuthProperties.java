package com.cuupe.shinkou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * 登录失败最大次数。
     * 达到该次数后，写入 Redis: auth:login_lock:{email}
     */
    private Integer loginFailMaxCount = 5;

    /**
     * 登录失败锁定时间，单位秒。
     */
    private Long loginFailLockSeconds = 300L;
}
