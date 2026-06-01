package com.cuupe.shinkou.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host:localhost}")
    private String host;
    @Value("${spring.data.redis.port:6379}")
    private int port;
    @Value("${spring.data.redis.database:0}")
    private int database;
    @Value("${spring.data.redis.password:}")
    private String password;

    /**
     * 创建Redisson客户端Bean
     * @return RedissonClient实例
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        String address = "redis://" + host + ":" + port;

        var singleServerConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(database);

        if (password != null && !password.isBlank()) {
            singleServerConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
