package com.eventory.expoUser.service;

import com.eventory.expoUser.dto.ExpoMainPageResponseDto;

import java.util.List;

public interface ExpoService {
    List<ExpoMainPageResponseDto> getMainPageExpos();
}
