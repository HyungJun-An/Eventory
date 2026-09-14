package com.eventory.expoUser.service;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.ExpoStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoUserRepository;
import com.eventory.expoUser.dto.ExpoDetailResponseDto;
import com.eventory.expoUser.dto.ExpoMainPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoServiceImpl implements ExpoService {

    private final ExpoUserRepository expoUserRepository;

    @Override
    public List<ExpoMainPageResponseDto> getMainPageExpos() {
        LocalDate today = LocalDate.now();
        List<Expo> expos = expoUserRepository.findAllVisibleAndNotEnded(today);

        // DTO 매핑 수정
        return expos.stream()
                .map(expo -> ExpoMainPageResponseDto.builder()
                        .expoId(expo.getExpoId())
                        .expoName(expo.getTitle())
                        .thumbnailUrl(expo.getImageUrl())
                        .location(expo.getLocation())
                        .startDate(expo.getStartDate().toString())
                        .endDate(expo.getEndDate().toString())
                        .categories(
                                expo.getExpoCategories().stream()
                                        .map(ec -> ec.getCategory().getName())
                                        .toList())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true) // 카테고리(지연 로딩) 접근
    public ExpoDetailResponseDto getExpoDetail(Long expoId) {
        // 승인되고 공개된 박람회만 참관객에게 노출
        Expo expo = expoUserRepository.findById(expoId)
                .filter(e -> e.getStatus() == ExpoStatus.APPROVED && Boolean.TRUE.equals(e.getVisibility()))
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
        ExpoAdmin admin = expo.getExpoAdmin();

        return ExpoDetailResponseDto.builder()
                .expoId(expo.getExpoId())
                .title(expo.getTitle())
                .description(expo.getDescription())
                .imageUrl(expo.getImageUrl())
                .location(expo.getLocation())
                .startDate(expo.getStartDate().toString())
                .endDate(expo.getEndDate().toString())
                .price(expo.getPrice())
                .remainingCapacity(Math.max(0, expo.getMaxCapacity() - expo.getReservedCount()))
                .categories(expo.getExpoCategories().stream().map(ec -> ec.getCategory().getName()).toList())
                .host(admin != null ? admin.getName() : null)
                .contact(admin != null ? admin.getEmail() : null)
                .build();
    }
}
