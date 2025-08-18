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

    // 로그인
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "M003", "아이디 또는 비밀번호가 틀렸습니다"),
    LOGIN_INPUT_INVALID(HttpStatus.BAD_REQUEST, "M004", "아이디와 비밀번호를 입력해주세요."),

    // 회원가입
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "M005", "이미 사용 중인 아이디입니다"),
    USER_REQUIRED_FIELDS_MISSING(HttpStatus.BAD_REQUEST, "M006", "일반 사용자의 필수 항목이 누락되었습니다"),
    COMPANY_REQUIRED_FIELDS_MISSING(HttpStatus.BAD_REQUEST, "M007", "참가 업체의 필수 항목이 누락되었습니다"),
    UNSUPPORTED_USER_TYPE(HttpStatus.BAD_REQUEST, "M008", "지원하지 않는 사용자 유형입니다"),
    USER_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "M009", "존재하지 않는 사용자 유형입니다"),

    // 리프레시 토큰 A
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 RefreshToken입니다"),
    REFRESH_TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "A002", "RefreshToken이 서버와 일치하지 않습니다"),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 AccessToken입니다"),
    LOGGED_OUT_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "해당 토큰은 로그아웃 처리된 토큰입니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "A005", "접근 권한이 없습니다."),
    EXPO_ADMIN_EXPIRED(HttpStatus.FORBIDDEN, "A006", "박람회 관리자 계정의 유효기간이 만료되었습니다"),

    // 회계 관리자 R
    NOT_FOUND_RESERVATION(HttpStatus.NOT_FOUND, "R001", "예약 정보가 없습니다."),
    NOT_FOUNT_STATISTICS(HttpStatus.NOT_FOUND, "R002", "지원하지 않는 range 값입니다."),
    NOT_FOUNT_RANGE(HttpStatus.NOT_FOUND, "R003", "지원하지 않는 range 값입니다."),
    NOT_FOUND_REFUND(HttpStatus.NOT_FOUND, "R004", "환불 정보가 없습니다."),
    NOT_FOUND_REASON(HttpStatus.BAD_REQUEST, "R005", "반려 사유가 누락되었습니다."),
    EXCEL_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "R006", "엑셀 생성에 실패했습니다."),

	// 박람회 E
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "카테고리가 존재하지 않습니다"),
	EXPO_NOT_FOUND(HttpStatus.NOT_FOUND, "E002", "박람회가 존재하지 않습니다"),
	HANDLED_EXPO(HttpStatus.BAD_REQUEST, "E003", "이미 처리된 박람회입니다"),
	REASON_REQUIRED(HttpStatus.BAD_REQUEST, "E004", "거절 사유가 필요합니다"),

    // 박람회 관리자 D
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "D001", "유효하지 않은 통계 기간입니다."),
    INVALID_ID(HttpStatus.BAD_REQUEST, "D002", "유효하지 않은 식별자입니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "D003", "유효하지 않은 날짜 구간입니다."),
    REPORT_EXPORT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "D004", "리포트 내보내기에 실패했습니다."),
    TICKET_TYPE_STATS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "D005", "티켓 타입 통계 집계 중 오류가 발생했습니다."),
    NOT_FOUND_EXPO_ADMIN(HttpStatus.NOT_FOUND, "D006", "박람회 관리자 계정을 찾을 수 없습니다"),

    // 박람회 관리자 마이페이지 P
    NOT_FOUND_MANAGER(HttpStatus.NOT_FOUND, "P001", "해당 박람회 담당자를 찾을 수 없습니다."),
    NOT_FOUND_EXPO(HttpStatus.NOT_FOUND, "P002", "해당 박람회를 찾을 수 없습니다."),
    NOT_FOUND_BANNER(HttpStatus.NOT_FOUND, "P003", "해당 배너를 찾을 수 없습니다."),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "P004", "해당 박람회에 접근 권한이 없습니다");

    private final HttpStatus status;
    private final String errorCode;
    private final String message;
}