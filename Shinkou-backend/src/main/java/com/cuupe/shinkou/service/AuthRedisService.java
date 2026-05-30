package com.cuupe.shinkou.service;

public interface AuthRedisService {
    void saveLoginToken(Long userId, String tokenId, long expiresInSeconds);

    boolean hasLoginToken(Long userId, String tokenId);

    boolean isLoginLocked(String email);

    Long getLoginLockTtl(String email);

    long increaseLoginFailCount(String email);

    void lockLogin(String email);

    void clearLoginFail(String email);
}
