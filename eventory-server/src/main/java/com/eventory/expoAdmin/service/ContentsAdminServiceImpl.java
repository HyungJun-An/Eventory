package com.eventory.expoAdmin.service;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoCategory;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.expoAdmin.dto.ContentsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentsAdminServiceImpl implements ContentsAdminService {

    private final ExpoRepository expoRepository;

    @Override
    public ContentsResponseDto findExpoContents(Long expoId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new RuntimeException("Expo not found"));

        // ExpoCategory를 통해 Category 이름 리스트 추출
        var categoryNames = expo.getExpoCategories().stream()
                .map(ExpoCategory::getCategory)       // ExpoCategory → Category
                .map(c -> c != null ? c.getName() : null) // null 체크
                .collect(Collectors.toList());

        return new ContentsResponseDto(
                expo.getExpoId(),
                expo.getTitle(),
                expo.getDescription(),
                expo.getImageUrl(),
                expo.getLocation(),
                expo.getExpoAdmin() !=null ? expo.getExpoAdmin().getPhone() :null,
                expo.getExpoAdmin() !=null ? expo.getExpoAdmin().getEmail() :null,
                expo.getPrice(),
                expo.getStartDate(),
                expo.getEndDate(),
                expo.getExpoAdmin() != null ? expo.getExpoAdmin().getName() : null,
                categoryNames
        );
    }
}
