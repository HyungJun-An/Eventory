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

    @Override
    public void blacklistAccessToken(String accessToken, long ttlMillis) {
        if (ttlMillis <= 0) ttlMillis = 1_000; // 최소 TTL 방어
        redis.opsForValue().set(KEY_BLACKLIST + accessToken, "1", ttlMillis, TimeUnit.MILLISECONDS);
    }
    @Override
    public boolean isBlacklisted(String accessToken) {
        return redis.hasKey(KEY_BLACKLIST + accessToken);
    }
    @Override
    public void saveRefreshToken(Long userId, String refreshToken, long ttlMillis) {
        redis.opsForValue().set(KEY_REFRESH + userId, refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
    }
    @Override
    public String findRefreshToken(Long userId) {
        return redis.opsForValue().get(KEY_REFRESH + userId);
    }
    @Override
    public void deleteRefreshToken(Long userId) {
        redis.delete(KEY_REFRESH + userId);
    }
}
