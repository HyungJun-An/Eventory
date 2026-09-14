package com.eventory.systemAdmin.dto;

/**
 * 새로 발급한 박람회관리자 로그인 정보 — 발급 직후 시스템관리자에게 한 번만 보여준다.
 * (서버에는 BCrypt 해시만 저장되므로 다시 조회할 수 없다)
 */
public record AdminCredentialDto(String loginId, String temporaryPassword) {
}
