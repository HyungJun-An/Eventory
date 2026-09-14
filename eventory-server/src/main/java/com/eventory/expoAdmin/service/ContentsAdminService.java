package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.ContentsResponseDto;

public interface ContentsAdminService {
    // 로그인한 관리자가 담당하는 박람회의 콘텐츠 조회
    ContentsResponseDto findExpoContents(Long expoAdminId, Long expoId);
}
