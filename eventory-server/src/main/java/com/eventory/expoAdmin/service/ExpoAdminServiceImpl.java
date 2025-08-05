package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.repository.ExpoAdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpoAdminServiceImpl implements ExpoAdminService {

    private final ExpoAdminRepository expoAdminRepository;

    @Override
    public List<ExpoResponseDto> findAllExpos() {
        return null;
    }
}
