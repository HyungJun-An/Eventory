package com.eventory.expoAdmin.service;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoCategory;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.expoAdmin.dto.ContentsResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ContentsAdminServiceImpl implements ContentsAdminService {

    private final ExpoRepository expoRepository;

    @Override
    @Transactional(readOnly = true) // 카테고리(지연 로딩) 접근을 위해 트랜잭션 안에서 조회
    public ContentsResponseDto findExpoContents(Long expoAdminId, Long expoId) {
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        if (expo.getExpoAdmin() == null || !expo.getExpoAdmin().getExpoAdminId().equals(expoAdminId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_ACCESS);
        }

        // ExpoCategory를 통해 Category 이름 리스트 추출
        List<String> categoryNames = expo.getExpoCategories().stream()
                .map(ExpoCategory::getCategory)
                .filter(Objects::nonNull)
                .map(c -> c.getName())
                .toList();

        return new ContentsResponseDto(
                expo.getExpoId(),
                expo.getTitle(),
                expo.getDescription(),
                expo.getImageUrl(),
                expo.getLocation(),
                expo.getExpoAdmin().getPhone(),
                expo.getExpoAdmin().getEmail(),
                expo.getPrice(),
                expo.getStartDate(),
                expo.getEndDate(),
                expo.getExpoAdmin().getName(),
                categoryNames
        );
    }
}
