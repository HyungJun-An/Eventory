package com.eventory.auth.tokenStore;

public interface TokenStore {
    void blacklistAccessToken(String accessToken, long ttlMillis);
    boolean isBlacklisted(String accessToken);
    void saveRefreshToken(Long userId, String refreshToken, long ttlMillis);
    String findRefreshToken(Long userId);
    void deleteRefreshToken(Long userId);
}
