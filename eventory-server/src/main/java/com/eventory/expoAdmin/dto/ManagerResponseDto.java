package com.eventory.expoAdmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/** 박람회 담당자 정보 응답 — 비밀번호(해시 포함)는 절대 내려주지 않는다 */
@Getter
@Builder
@AllArgsConstructor
public class ManagerResponseDto {

    private Long expoAdminId;

    private String name;

    private String email;

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
