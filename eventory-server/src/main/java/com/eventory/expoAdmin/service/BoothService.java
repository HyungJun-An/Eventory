package com.eventory.expoAdmin.service;

import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.expoAdmin.dto.BoothRequestDto;
import com.eventory.expoAdmin.dto.BoothResponseDto;

import java.util.List;

public interface BoothService {
    List<BoothResponseDto> findAllBooths(Long expoAdminId, Long expoId);
    void updateBooth(Long expoAdminId, Long expoId, Long boothId, BoothRequestDto requestDto);
}
