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

    // 회계 관리자 R
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "R001", "예약 정보가 없습니다.");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}
