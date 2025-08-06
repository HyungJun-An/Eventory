package com.eventory.expoAdmin.service.mapper;

import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.entity.Expo;
import org.springframework.stereotype.Component;

@Component
public class ExpoMapper {
    public ExpoResponseDto toDto(Expo expo) {
        return ExpoResponseDto.builder()
                .expoId(expo.getExpoId())
                .expoAdminId(expo.getExpoAdmin().getExpoAdminId())
                .title(expo.getTitle())
                .description(expo.getDescription())
                .startDate(expo.getStartDate())
                .endDate(expo.getEndDate())
                .location(expo.getLocation())
                .visibility(expo.getVisibility())
                .build();
    }
}
