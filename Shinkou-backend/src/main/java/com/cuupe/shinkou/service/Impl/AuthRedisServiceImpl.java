package com.cuupe.shinkou.service.Impl;

import com.cuupe.shinkou.config.AuthProperties;
import com.cuupe.shinkou.service.AuthRedisService;
import com.cuupe.shinkou.util.AuthRedisKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthRedisServiceImpl implements AuthRedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    @Override
    public void saveLoginToken(Long userId, String tokenId, long expiresInSeconds) {
        stringRedisTemplate.opsForValue().set(
                AuthRedisKeys.tokenKey(userId, tokenId),
                "1",
                Duration.ofSeconds(expiresInSeconds)
        );
    }

    @Override
    public boolean hasLoginToken(Long userId, String tokenId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(AuthRedisKeys.tokenKey(userId, tokenId))
        );
    }

    @Override
    public boolean isLoginLocked(String email) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(AuthRedisKeys.loginLockKey(email))
        );
    }

    @Override
    public Long getLoginLockTtl(String email) {
        return stringRedisTemplate.getExpire(AuthRedisKeys.loginLockKey(email));
    }

    @Override
    public long increaseLoginFailCount(String email) {
        String key = AuthRedisKeys.loginFailKey(email);

        Long count = stringRedisTemplate.opsForValue().increment(key);

        if (count != null && count == 1L) {
            stringRedisTemplate.expire(
                    key,
                    Duration.ofSeconds(authProperties.getLoginFailLockSeconds())
            );
        }

        return count == null ? 0L : count;
    }

    @Override
    public void lockLogin(String email) {
        stringRedisTemplate.opsForValue().set(
                AuthRedisKeys.loginLockKey(email),
                "1",
                Duration.ofSeconds(authProperties.getLoginFailLockSeconds())
        );
    }

    @Override
    public void clearLoginFail(String email) {
        stringRedisTemplate.delete(AuthRedisKeys.loginFailKey(email));
        stringRedisTemplate.delete(AuthRedisKeys.loginLockKey(email));
    }
}
