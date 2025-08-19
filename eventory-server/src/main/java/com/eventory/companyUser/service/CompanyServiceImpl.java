package com.eventory.companyUser.service;

import com.eventory.auth.repository.UserRepository;
import com.eventory.common.entity.Booth;
import com.eventory.common.entity.User;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.BoothRepository;
import com.eventory.companyUser.dto.BoothRequestDto;
import com.eventory.companyUser.dto.BoothResponseDto;
import com.eventory.companyUser.dto.UserRequestDto;
import com.eventory.companyUser.dto.UserResponseDto;
import com.eventory.companyUser.service.mapper.CompanyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private UserRepository userRepository;
    private CompanyMapper companyMapper;
    private BoothRepository boothRepository;

    // 부스 신청
    @Override
    public void createBooth(Long companyUserId, BoothRequestDto requestDto) {

        // 기본키(companyUserId)로 User 조회
        User user = userRepository.findById(companyUserId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_COMPANY));

        // 참가 업체 계정인지 확인
        validateCompanyUser(user);

        Booth booth = companyMapper.toBoothRequestDto(user, requestDto);

        boothRepository.save(booth);
    }

    // 내 정보 조회
    @Override
    public UserResponseDto findProfile(Long companyUserId) {

        // 기본키(companyUserId)로 User 조회
        User user = userRepository.findById(companyUserId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_COMPANY));

        // 참가 업체 계정인지 확인
        validateCompanyUser(user);

        // dto객체로 변환 및 반환
        return companyMapper.toUserResponseDto(user);
    }

    // 내 정보 수정
    @Override
    public void updateProfile(Long companyUserId, UserRequestDto requestDto) {

        // 기본키(companyUserId)로 User 조회
        User user = userRepository.findById(companyUserId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_COMPANY));

        // 참가 업체 정보 업데이트
        user.updateUser(requestDto);

        // 수정한 참가 업체 정보 저장
        userRepository.save(user);
    }

    // 부스 목록 조회
    @Override
    public List<BoothResponseDto> findAllBooths(Long companyUserId) {

        // 기본키(companyUserId)로 User 조회
        User user = userRepository.findById(companyUserId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_COMPANY));

        // 참가 업체 계정인지 확인
        validateCompanyUser(user);

        List<Booth> booths = boothRepository.findAllByUser(user);

        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return booths.stream()
                .map(companyMapper::toBoothResponseDto)
                .collect(Collectors.toList());
    }

    // 부스 상세 조회
    @Override
    public BoothResponseDto findBooth(Long companyUserId, Long boothId) {

        // 기본키(companyUserId)로 User 조회
        User user = userRepository.findById(companyUserId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_COMPANY));

        // 참가 업체 계정인지 확인
        validateCompanyUser(user);

        // 기본키(boothId)로 Booth 조회
        Booth booth = boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BOOTH));

        // 부스 담당 참가업체인지 확인
        if (!Objects.equals(booth.getUser().getUserId(), companyUserId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_COMPANY_USER);
        }

        // dto 객체로 변환 및 반환
        return companyMapper.toBoothResponseDto(booth);
    }

    // 특정 부스 정보 수정
    @Override
    public void updateBooth(Long companyUserId, Long boothId, BoothRequestDto requestDto) {

        // 기본키(companyUserId)로 User 조회
        User user = userRepository.findById(companyUserId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_COMPANY));

        // 참가 업체 계정인지 확인
        validateCompanyUser(user);

        // 기본키(boothId)로 Booth 조회
        Booth booth = boothRepository.findById(boothId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_BOOTH));

        // 부스 담당 참가업체인지 확인
        if (!Objects.equals(booth.getUser().getUserId(), companyUserId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_COMPANY_USER);
        }

        // 부스 정보 변경 및 저장
        booth.updateBooth(requestDto);

        boothRepository.save(booth);
    }

    // 참가 업체 계정인지 확인
    private void validateCompanyUser(User user) {
        if (!Objects.equals(user.getUserType().getTypeId(), 3L)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_COMPANY_USER);
        }
    }
}
