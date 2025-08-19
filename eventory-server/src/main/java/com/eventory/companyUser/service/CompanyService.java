package com.eventory.companyUser.service;

import com.eventory.companyUser.dto.BoothRequestDto;
import com.eventory.companyUser.dto.BoothResponseDto;
import com.eventory.companyUser.dto.UserRequestDto;
import com.eventory.companyUser.dto.UserResponseDto;

import java.util.List;

public interface CompanyService {
    void createBooth(Long companyUserId, BoothRequestDto requestDto);

    UserResponseDto findProfile(Long companyUserId);

    void updateProfile(Long companyUserId, UserRequestDto requestDto);

    List<BoothResponseDto> findAllBooths(Long companyUserId);

    BoothResponseDto findBooth(Long companyUserId, Long boothId);

    void updateBooth(Long companyUserId, Long boothId, BoothRequestDto requestDto);
}
