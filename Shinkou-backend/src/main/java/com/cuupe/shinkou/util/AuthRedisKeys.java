package com.cuupe.shinkou.util;

public final class AuthRedisKeys {

    private AuthRedisKeys() {
    }

    public static String tokenKey(Long userId, String tokenId) {
        return "auth:token:" + userId + ":" + tokenId;
    }

    public static String blacklistKey(String tokenId) {
        return "auth:blacklist:" + tokenId;
    }

    public static String loginFailKey(String email) {
        return "auth:login_fail:" + email;
    }

    public static String loginLockKey(String email) {
        return "auth:login_lock:" + email;
    }
}