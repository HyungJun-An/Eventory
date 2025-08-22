package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.*;
import jakarta.validation.Valid;

public interface ExpoInfoService {
    ManagerResponseDto findExpoManagerInfo(Long expoAdminId);

    void updateExpoManagerInfo(Long expoAdminId, @Valid ManagerRequestDto requestDto);

    ExpoResponseDto findExpoInfo(Long expoAdminId, Long expoId);

    void updateExpoInfo(Long expoAdminId, Long expoId, @Valid ExpoUpdateRequestDto requestDto);

    void createExpoBanner(Long expoAdminId, Long expoId, @Valid BannerCreateRequestDto requestDto);

    BannerResponseDto findExpoBanner(Long expoAdminId, Long expoId);

    void updateExpoBanner(Long expoAdminId, Long expoId, @Valid BannerUpdateRequestDto requestDto);
}
