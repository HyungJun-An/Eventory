package com.eventory.expoAdmin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.Nullable;

//csv, 엑셀 파일명 생성
public record FileDownloadDto(

        @NotNull(message = "파일 내용(content)은 null일 수 없습니다.")
        @NotEmpty(message = "파일 내용(content)은 비어 있을 수 없습니다.")
        byte[] content,

        @NotBlank(message = "파일명(fileName)은 비어 있을 수 없습니다.")
        String fileName,

        @Nullable
        String contentType
) { }
