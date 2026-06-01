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

    /**
     * 保存登录令牌到Redis
     * @param userId 用户ID
     * @param tokenId 令牌ID
     * @param expiresInSeconds 过期时间（秒）
     */
    @Override
    public void saveLoginToken(Long userId, String tokenId, long expiresInSeconds) {
        stringRedisTemplate.opsForValue().set(
                AuthRedisKeys.tokenKey(userId, tokenId),
                "1",
                Duration.ofSeconds(expiresInSeconds)
        );
    }

    /**
     * 登出用户，删除Redis中的令牌
     * @param userId 用户ID
     * @param tokenId 令牌ID
     * @return 是否成功删除
     */
    @Override
    public boolean logout(Long userId, String tokenId) {
        stringRedisTemplate.delete(AuthRedisKeys.tokenKey(userId, tokenId));
        return true;
    }

    /**
     * 检查Redis中是否存在有效的登录令牌
     * @param userId 用户ID
     * @param tokenId 令牌ID
     * @return 是否存在有效令牌
     */
    @Override
    public boolean hasLoginToken(Long userId, String tokenId) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(AuthRedisKeys.tokenKey(userId, tokenId))
        );
    }

    /**
     * 检查邮箱是否被锁定登录
     * @param email 用户邮箱
     * @return 是否被锁定
     */
    @Override
    public boolean isLoginLocked(String email) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(AuthRedisKeys.loginLockKey(email))
        );
    }

    /**
     * 获取登录锁定的剩余时间
     * @param email 用户邮箱
     * @return 剩余时间（秒），null表示未锁定
     */
    @Override
    public Long getLoginLockTtl(String email) {
        return stringRedisTemplate.getExpire(AuthRedisKeys.loginLockKey(email));
    }

    /**
     * 增加登录失败次数
     * @param email 用户邮箱
     * @return 当前失败次数
     */
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

    /**
     * 锁定用户登录（达到最大失败次数后）
     * @param email 用户邮箱
     */
    @Override
    public void lockLogin(String email) {
        stringRedisTemplate.opsForValue().set(
                AuthRedisKeys.loginLockKey(email),
                "1",
                Duration.ofSeconds(authProperties.getLoginFailLockSeconds())
        );
    }

    /**
     * 清除登录失败记录和锁定状态
     * @param email 用户邮箱
     */
    @Override
    public void clearLoginFail(String email) {
        stringRedisTemplate.delete(AuthRedisKeys.loginFailKey(email));
        stringRedisTemplate.delete(AuthRedisKeys.loginLockKey(email));
    }
}
