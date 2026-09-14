package com.eventory.auth.tokenStore;

import java.util.Optional;

public interface TokenStore {
    void blacklistAccessToken(String accessToken, long ttlMillis);

    boolean isBlacklisted(String accessToken);

    /** 계정의 리프레시 토큰 저장 — 이전 토큰은 폐기된다 (계정당 1개) */
    void saveRefreshToken(AccountType type, Long id, String refreshToken, long ttlMillis);

    /** 리프레시 토큰의 소유자. 만료·폐기(재발급·로그아웃)된 토큰이면 empty */
    Optional<RefreshTokenOwner> findOwner(String refreshToken);

    void deleteRefreshToken(AccountType type, Long id);
}
