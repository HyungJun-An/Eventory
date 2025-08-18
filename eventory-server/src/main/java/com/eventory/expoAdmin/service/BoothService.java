package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.BoothRequestDto;
import com.eventory.expoAdmin.dto.BoothResponseDto;

import java.util.List;

public interface BoothService {
    List<BoothResponseDto> findAllBooths(Long expoId);
    void updateBooth(Long expoId, Long boothId, BoothRequestDto requestDto);
}
