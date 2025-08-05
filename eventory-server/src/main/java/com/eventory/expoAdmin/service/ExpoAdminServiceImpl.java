package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.entity.Expo;
import com.eventory.expoAdmin.repository.ExpoAdminRepository;
import com.eventory.expoAdmin.repository.ExpoRepository;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpoAdminServiceImpl implements ExpoAdminService {

    private final ExpoAdminRepository expoAdminRepository;
    private final ExpoRepository expoRepository;
    private final ExpoMapper expoMapper;

    @Override
    public List<ExpoResponseDto> findAllExpos(Long expoAdminId) {
        List<Expo> expos = expoRepository.findByExpoAdminIdOrderByTitleAsc(expoAdminId);
        return expos.stream()
                .map(expoMapper::toDto)
                .collect(Collectors.toList());
    }
}
