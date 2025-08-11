package com.eventory.expoAdmin.dto;

import org.springframework.lang.Nullable;

//csv, 엑셀 파일명 생성
public record FileDownloadDto(
        byte[] content,
        String fileName,
        @Nullable String contentType
) { }
