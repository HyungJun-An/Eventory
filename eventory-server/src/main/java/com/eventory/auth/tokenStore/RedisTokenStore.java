package com.eventory.auth.tokenStore;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisTokenStore implements TokenStore {
    private final StringRedisTemplate redis;

    private static final String KEY_BLACKLIST = "blacklist:";  // blacklist:<access>
    private static final String KEY_REFRESH = "refresh:user:"; // refresh:user:<userId>
    private static final String KEY_USER_BY_REFRESH = "user:refresh:";// user:refresh:<refresh> -> <userId>

    @Override
    public void blacklistAccessToken(String accessToken, long ttlMillis) {
        if (accessToken == null) return;
        if (ttlMillis <= 0) ttlMillis = 1_000; // 최소 TTL 방어
        redis.opsForValue().set(KEY_BLACKLIST + accessToken, "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String accessToken) {
        return accessToken != null && redis.hasKey(KEY_BLACKLIST + accessToken);
//        return redis.hasKey(KEY_BLACKLIST + accessToken);
    }

    @Override
    public void saveRefreshToken(Long userId, String refreshToken, long ttlMillis) {
        if (userId == null || refreshToken == null) return;
        // 1) userId -> refresh
        redis.opsForValue().set(KEY_REFRESH + userId, refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
        // 2) refresh -> userId (역인덱스)
        redis.opsForValue().set(KEY_USER_BY_REFRESH + refreshToken, String.valueOf(userId), ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public String findRefreshToken(Long userId) {
        if (userId == null) return null;
        return redis.opsForValue().get(KEY_REFRESH + userId);
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        if (userId == null) return;
        String refresh = findRefreshToken(userId);
        if (refresh != null) {
            redis.delete(KEY_USER_BY_REFRESH + refresh);
        }
        redis.delete(KEY_REFRESH + userId);
    }

    // ===  refresh 값으로 userId 조회 ===
    @Override
    public Long findUserIdByRefresh(String refreshToken) {
        if (refreshToken == null) return null;
        String v = redis.opsForValue().get(KEY_USER_BY_REFRESH + refreshToken);
        return (v == null ? null : Long.valueOf(v));
    }


    // ===  특정 refresh 인덱스 제거 ===
    @Override
    public void deleteByRefresh(String refreshToken) {
        Long uid = findUserIdByRefresh(refreshToken);
        if (uid != null) {
            redis.delete(KEY_REFRESH + uid);
        }
        redis.delete(KEY_USER_BY_REFRESH + refreshToken);
    }
}
