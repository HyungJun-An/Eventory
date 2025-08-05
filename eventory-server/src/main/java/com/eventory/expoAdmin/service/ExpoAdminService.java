package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.ExpoResponseDto;

import java.util.List;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos();
}
