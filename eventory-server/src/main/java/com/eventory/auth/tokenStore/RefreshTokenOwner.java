package com.eventory.auth.tokenStore;

/** 리프레시 토큰의 주인 (계정 종류 + 해당 테이블의 id) */
public record RefreshTokenOwner(AccountType type, Long id) {
}
