package com.eventory.auth.tokenStore;

/**
 * 리프레시 토큰을 소유한 계정 종류.
 * user / expo_admin / system_admin 테이블은 id 가 각각 1부터 시작해 서로 겹치므로
 * id 만 저장하면 참관객 토큰으로 같은 번호의 관리자 토큰을 재발급받을 수 있다 (권한 상승).
 */
public enum AccountType {
    USER,
    EXPO_ADMIN,
    SYSTEM_ADMIN
}
