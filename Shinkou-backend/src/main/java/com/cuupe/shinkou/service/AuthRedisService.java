package com.cuupe.shinkou.service;

public interface AuthRedisService {
    /**
     * 保存登录令牌到Redis
     * @param userId 用户ID
     * @param tokenId 令牌ID
     * @param expiresInSeconds 过期时间（秒）
     */
    void saveLoginToken(Long userId, String tokenId, long expiresInSeconds);

    /**
     * 登出用户，删除Redis中的令牌
     * @param userId 用户ID
     * @param tokenId 令牌ID
     * @return 是否成功删除
     */
    boolean logout(Long userId, String tokenId);

    /**
     * 检查Redis中是否存在有效的登录令牌
     * @param userId 用户ID
     * @param tokenId 令牌ID
     * @return 是否存在有效令牌
     */
    boolean hasLoginToken(Long userId, String tokenId);

    /**
     * 检查邮箱是否被锁定登录
     * @param email 用户邮箱
     * @return 是否被锁定
     */
    boolean isLoginLocked(String email);

    /**
     * 获取登录锁定的剩余时间
     * @param email 用户邮箱
     * @return 剩余时间（秒），null表示未锁定
     */
    Long getLoginLockTtl(String email);

    /**
     * 增加登录失败次数
     * @param email 用户邮箱
     * @return 当前失败次数
     */
    long increaseLoginFailCount(String email);

    /**
     * 锁定用户登录（达到最大失败次数后）
     * @param email 用户邮箱
     */
    void lockLogin(String email);

    /**
     * 清除登录失败记录和锁定状态
     * @param email 用户邮箱
     */
    void clearLoginFail(String email);
}
