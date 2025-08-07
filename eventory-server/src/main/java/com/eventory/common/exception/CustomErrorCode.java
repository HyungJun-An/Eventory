package com.eventory.common.exception;

import org.springframework.http.HttpStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CustomErrorCode {
    // 회원 M
    MEMBER_NOT_EXIST(HttpStatus.BAD_REQUEST, "M001", "존재하지 않는 회원입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "M002", "다시 작성해주세요"),

    // 로그인 관련 추가
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "M003", "비밀번호가 일치하지 않습니다."),
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "M004", "아이디와 비밀번호를 입력해주세요.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}