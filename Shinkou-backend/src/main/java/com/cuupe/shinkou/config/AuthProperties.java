package com.cuupe.shinkou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    /**
     * 登录失败最大次数。
     * 达到该次数后，写入 Redis: auth:login_lock:{email}
     * @return 最大失败次数
     */
    private Integer loginFailMaxCount = 5;

    /**
     * 登录失败锁定时间，单位秒。
     * @return 锁定时间（秒）
     */
    private Long loginFailLockSeconds = 300L;
}
