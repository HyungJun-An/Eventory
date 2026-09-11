package com.eventory.systemAdmin.dto;

/** 로그인한 시스템관리자 정보 (헤더 표시용) */
public record SysMeResponseDto(Long id, String loginId, String name, String email) {
}
