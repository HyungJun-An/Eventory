package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.ContentsResponseDto;

public interface ContentsAdminService {

    // Expo ID로 ContentsResponseDto 가져오기
    ContentsResponseDto findExpoContents(Long expoId);

}
