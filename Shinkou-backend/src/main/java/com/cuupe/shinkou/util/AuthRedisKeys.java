package com.cuupe.shinkou.util;

/**
 * 验证用 Redis Key 生成工具类
 */
public final class AuthRedisKeys {

    /**
     * 生成用户登录令牌的Redis Key
     * @param userId 用户ID
     * @param tokenId 令牌ID
     * @return Redis Key字符串
     */
    public static String tokenKey(Long userId, String tokenId) {
        return "auth:token:" + userId + ":" + tokenId;
    }

    /**
     * 生成令牌黑名单的Redis Key
     * @param tokenId 令牌ID
     * @return Redis Key字符串
     */
    public static String blacklistKey(String tokenId) {
        return "auth:blacklist:" + tokenId;
    }

    /**
     * 生成登录失败计数的Redis Key
     * @param email 用户邮箱
     * @return Redis Key字符串
     */
    public static String loginFailKey(String email) {
        return "auth:login_fail:" + email;
    }

    /**
     * 生成登录锁定状态的Redis Key
     * @param email 用户邮箱
     * @return Redis Key字符串
     */
    public static String loginLockKey(String email) {
        return "auth:login_lock:" + email;
    }
}