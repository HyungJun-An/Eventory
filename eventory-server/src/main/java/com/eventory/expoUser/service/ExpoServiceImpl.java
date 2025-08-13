package com.eventory.expoUser.service;

import com.eventory.common.entity.Expo;
import com.eventory.common.repository.ExpoUserRepository;
import com.eventory.expoUser.dto.ExpoMainPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
