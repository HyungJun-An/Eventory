package com.eventory.expoAdmin.service;

import com.eventory.common.entity.Booth;
import com.eventory.common.entity.BoothStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.BoothRepository;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.expoAdmin.dto.BoothRequestDto;
import com.eventory.expoAdmin.dto.BoothResponseDto;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoothServiceImpl implements BoothService {

    private final ExpoRepository expoRepository;
    private final BoothRepository boothRepository;
    private final ExpoMapper expoMapper;

    @Override
    public List<BoothResponseDto> findAllBooths(Long expoId) {
        List<Booth> booths = boothRepository.findAllByExpo_ExpoId(expoId);
        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return booths.stream()
                .map(expoMapper::toBoothResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateBooth(Long expoId, Long boothId, BoothRequestDto requestDto) {

        // 부스 조회
        Booth booth = boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BOOTH));

        // 부스 반려 시 반려 사유 없으면 오류 발생
        if (requestDto.getStatus() == BoothStatus.REJECTED && (requestDto.getReason() == null || requestDto.getReason().isBlank())) {
            throw new CustomException(CustomErrorCode.NOT_FOUND_REASON);
        }

        // 요청됨 상태라면 오류 발생
        if (requestDto.getStatus() == BoothStatus.PENDING) {
            throw new CustomException(CustomErrorCode.NOT_FOUND_BOOTH);
        }

        // 부스 상태 변경 및 저장
        booth.updateStatus(requestDto);
        boothRepository.save(booth);
    }
}
