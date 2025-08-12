package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.*;

import java.util.List;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos(Long expoAdminId);
}
