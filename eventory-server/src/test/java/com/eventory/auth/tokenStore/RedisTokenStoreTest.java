package com.eventory.auth.tokenStore;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/** 실제 Redis 에서 리프레시 토큰의 계정 종류 분리·교체·폐기를 검증 */
@Testcontainers
class RedisTokenStoreTest {

    private static final long TTL = 60_000;

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.0-alpine").withExposedPorts(6379);

    static LettuceConnectionFactory connectionFactory;
    RedisTokenStore store;

    @BeforeAll
    static void connect() {
        connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
    }

    @AfterAll
    static void close() {
        connectionFactory.destroy();
    }

    @BeforeEach
    void setUp() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushAll();
        }
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        template.afterPropertiesSet();
        store = new RedisTokenStore(template);
    }

    @Test
    @DisplayName("id 가 같아도 계정 종류가 다르면 서로 다른 토큰으로 구분된다 (권한 상승 방지의 핵심)")
    void sameIdDifferentAccountTypes() {
        store.saveRefreshToken(AccountType.USER, 1L, "user-token", TTL);
        store.saveRefreshToken(AccountType.SYSTEM_ADMIN, 1L, "admin-token", TTL);

        assertThat(store.findOwner("user-token")).contains(new RefreshTokenOwner(AccountType.USER, 1L));
        assertThat(store.findOwner("admin-token")).contains(new RefreshTokenOwner(AccountType.SYSTEM_ADMIN, 1L));
    }

    @Test
    @DisplayName("재발급으로 새 토큰을 저장하면 이전 토큰은 더 이상 쓸 수 없다 (토큰 회전)")
    void rotation() {
        store.saveRefreshToken(AccountType.EXPO_ADMIN, 3L, "old", TTL);
        store.saveRefreshToken(AccountType.EXPO_ADMIN, 3L, "new", TTL);

        assertThat(store.findOwner("old")).isEmpty();
        assertThat(store.findOwner("new")).contains(new RefreshTokenOwner(AccountType.EXPO_ADMIN, 3L));
    }

    @Test
    @DisplayName("로그아웃·비밀번호 재발급으로 삭제하면 토큰을 쓸 수 없다")
    void delete() {
        store.saveRefreshToken(AccountType.EXPO_ADMIN, 3L, "token", TTL);

        store.deleteRefreshToken(AccountType.EXPO_ADMIN, 3L);

        assertThat(store.findOwner("token")).isEmpty();
    }

    @Test
    @DisplayName("다른 계정 종류의 토큰을 지워도 영향을 받지 않는다")
    void deleteIsScopedToAccountType() {
        store.saveRefreshToken(AccountType.USER, 1L, "user-token", TTL);

        store.deleteRefreshToken(AccountType.EXPO_ADMIN, 1L);

        assertThat(store.findOwner("user-token")).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 토큰은 소유자가 없다")
    void unknownToken() {
        assertThat(store.findOwner("nope")).isEmpty();
        assertThat(store.findOwner(null)).isEmpty();
    }
}
