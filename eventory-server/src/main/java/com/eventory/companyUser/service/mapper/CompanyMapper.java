package com.eventory.companyUser.service.mapper;

import com.eventory.auth.repository.UserRepository;
import com.eventory.common.entity.Booth;
import com.eventory.common.entity.BoothStatus;
import com.eventory.common.entity.Expo;
import com.eventory.common.entity.User;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.companyUser.dto.BoothRequestDto;
import com.eventory.companyUser.dto.BoothResponseDto;
import com.eventory.companyUser.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CompanyMapper {

    private ExpoRepository expoRepository;
    private UserRepository userRepository;

    public Booth toBoothRequestDto(User user, BoothRequestDto requestDto) {

        Expo expo = expoRepository.findById(requestDto.getExpoId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

        return Booth.builder()
                .expo(expo)
                .user(user)
                .title(requestDto.getTitle())
                .imageUrl(requestDto.getImageUrl())
                .location(requestDto.getLocation())
                .managerName(requestDto.getManagerName())
                .department(requestDto.getDepartment())
                .phone(requestDto.getPhone())
                .email(requestDto.getEmail())
                .status(BoothStatus.PENDING)
                .build();
    }

    public UserResponseDto toUserResponseDto(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .userType(user.getUserType())
                .customerId(user.getCustomerId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .phone(user.getPhone())
                .companyNameKr(user.getCompanyNameKr())
                .companyNameEng(user.getCompanyNameEng())
                .ceoNameKr(user.getCeoNameKr())
                .ceoNameEng(user.getCeoNameEng())
                .companyAddress(user.getCompanyAddress())
                .registrationNum(user.getRegistrationNum())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public BoothResponseDto toBoothResponseDto(Booth booth) {
        return BoothResponseDto.builder()
                .boothId(booth.getBoothId())
                .expoId(booth.getExpo().getExpoId())
                .userId(booth.getUser().getUserId())
                .paymentId(booth.getPayment() != null ? booth.getPayment().getPaymentId() : null)
                .title(booth.getTitle())
                .imageUrl(booth.getImageUrl())
                .location(booth.getLocation())
                .managerName(booth.getManagerName())
                .department(booth.getDepartment())
                .phone(booth.getPhone())
                .email(booth.getEmail())
                .createdAt(booth.getCreatedAt())
                .updatedAt(booth.getUpdatedAt())
                .status(booth.getStatus())
                .build();
    }
}
