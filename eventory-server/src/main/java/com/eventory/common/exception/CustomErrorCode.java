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
    // 회원가입
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "M005", "이미 사용 중인 아이디입니다"),
    USER_REQUIRED_FIELDS_MISSING(HttpStatus.BAD_REQUEST, "M006", "일반 사용자의 필수 항목이 누락되었습니다"),
    COMPANY_REQUIRED_FIELDS_MISSING(HttpStatus.BAD_REQUEST, "M007", "참가 업체의 필수 항목이 누락되었습니다"),
    UNSUPPORTED_USER_TYPE(HttpStatus.BAD_REQUEST, "M008", "지원하지 않는 사용자 유형입니다"),
    USER_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "M009", "존재하지 않는 사용자 유형입니다"),


    // 로그인
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "M003", "아이디 또는 비밀번호가 틀렸습니다"),
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "M004", "아이디와 비밀번호를 입력해주세요."),

    // 회계 관리자 R
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "R001", "예약 정보가 없습니다."),
    NOT_FOUNT_STATISTICS(HttpStatus.NOT_FOUND, "R002", "지원하지 않는 range 값입니다."),
    NOT_FOUNT_RANGE(HttpStatus.NOT_FOUND, "R003", "지원하지 않는 range 값입니다.");



    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}