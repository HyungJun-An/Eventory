package com.eventory.expoAdmin.dto;

import com.eventory.common.entity.BoothStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 부스 승인/반려 요청.
 * 사유는 반려(REJECTED)일 때만 필수이며 BoothServiceImpl 에서 검증한다.
 * (기존 @NotBlank 때문에 사유 없는 승인 요청이 400 으로 막혔다)
 */
@Getter
@Builder
@AllArgsConstructor
public class BoothRequestDto {

    @NotNull
    private BoothStatus status;

    private String reason;
}
