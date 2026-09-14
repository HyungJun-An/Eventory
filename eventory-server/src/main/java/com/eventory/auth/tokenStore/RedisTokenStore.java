package com.eventory.auth.tokenStore;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis 토큰 저장소
 * - refresh:{TYPE}:{id}       → 리프레시 토큰 (계정당 1개)
 * - refresh:owner:{token}     → "{TYPE}:{id}" (역인덱스)
 * 계정 종류를 키에 포함해, 테이블마다 겹치는 id 로 다른 종류의 계정 토큰을 발급받는 것을 막는다.
 */
@Component
@RequiredArgsConstructor
public class RedisTokenStore implements TokenStore {
    private final StringRedisTemplate redis;

    // JwtAuthenticationFilter.BLACKLIST_PREFIX 와 반드시 같아야 한다 (다르면 로그아웃한 토큰이 계속 통과됨)
    private static final String KEY_BLACKLIST = "blacklist:access:";
    private static final String KEY_REFRESH = "refresh:";
    private static final String KEY_OWNER = "refresh:owner:";

    @Override
    public void blacklistAccessToken(String accessToken, long ttlMillis) {
        if (accessToken == null) return;
        if (ttlMillis <= 0) ttlMillis = 1_000; // 최소 TTL 방어
        redis.opsForValue().set(KEY_BLACKLIST + accessToken, "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return accessToken != null && Boolean.TRUE.equals(redis.hasKey(KEY_BLACKLIST + accessToken));
    }

    @Override
    public void saveRefreshToken(AccountType type, Long id, String refreshToken, long ttlMillis) {
        if (type == null || id == null || refreshToken == null) return;
        deleteRefreshToken(type, id); // 이전 토큰 폐기 (재발급 시 회전)
        redis.opsForValue().set(accountKey(type, id), refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
        redis.opsForValue().set(KEY_OWNER + refreshToken, type.name() + ":" + id, ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public Optional<RefreshTokenOwner> findOwner(String refreshToken) {
        if (refreshToken == null) return Optional.empty();
        String value = redis.opsForValue().get(KEY_OWNER + refreshToken);
        if (value == null) return Optional.empty();

        int sep = value.indexOf(':');
        RefreshTokenOwner owner = new RefreshTokenOwner(
                AccountType.valueOf(value.substring(0, sep)), Long.valueOf(value.substring(sep + 1)));
        // 계정의 현재 토큰과 일치할 때만 유효 (폐기된 토큰의 역인덱스가 남아 있어도 통과시키지 않음)
        String current = redis.opsForValue().get(accountKey(owner.type(), owner.id()));
        return refreshToken.equals(current) ? Optional.of(owner) : Optional.empty();
    }

    @Override
    public void deleteRefreshToken(AccountType type, Long id) {
        if (type == null || id == null) return;
        String token = redis.opsForValue().get(accountKey(type, id));
        if (token != null) {
            redis.delete(KEY_OWNER + token);
        }
        redis.delete(accountKey(type, id));
    }

    private String accountKey(AccountType type, Long id) {
        return KEY_REFRESH + type.name() + ":" + id;
    }
}
