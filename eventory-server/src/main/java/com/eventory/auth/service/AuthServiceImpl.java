package com.eventory.auth.service;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.dto.SignupRequest;
import com.eventory.auth.dto.SignupResponse;
import com.eventory.auth.repository.UserRepository;
import com.eventory.auth.repository.UserTypeRepository;

import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.auth.tokenStore.TokenStore;
import com.eventory.common.entity.User;
import com.eventory.common.entity.UserType;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStore tokenStore;

    // 회원가입
    @Override
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByCustomerId(request.getCustomerId())) {
            throw  new CustomException(CustomErrorCode.DUPLICATE_USER_ID);
        }


        // 1-시스템 관리자 2-박람회 관리자 3-참가업체 4-참관객
        if (request.getTypeId() == 4L) {
            if (request.getBirth() == null || request.getGender() == null || request.getPhone() == null) {
                throw new CustomException(CustomErrorCode.USER_REQUIRED_FIELDS_MISSING);
            }
        } else if (request.getTypeId() == 3L) {
            if (request.getCompanyNameKr() == null || request.getCompanyNameEng() == null ||
                    request.getCeoNameKr() == null || request.getCeoNameEng() == null ||
                    request.getCompanyAddress() == null || request.getRegistrationNum() == null) {
                throw new CustomException(CustomErrorCode.COMPANY_REQUIRED_FIELDS_MISSING);
            }
        } else {
            throw new CustomException(CustomErrorCode.UNSUPPORTED_USER_TYPE);
        }

        UserType userType = userTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.USER_TYPE_NOT_FOUND));

        User user = User.builder()
                .customerId(request.getCustomerId())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userType(userType)
                .birth(request.getBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .companyNameKr(request.getCompanyNameKr())
                .companyNameEng(request.getCompanyNameEng())
                .ceoNameKr(request.getCeoNameKr())
                .ceoNameEng(request.getCeoNameEng())
                .companyAddress(request.getCompanyAddress())
                .registrationNum(request.getRegistrationNum())
                .build();

        User saved = userRepository.save(user);

        return new SignupResponse(saved.getUserId(), "회원가입이 완료되었습니다.");
    }

    // 로그인
    @Override
    public LoginResponse login(LoginRequest request) {
        if (request.getCustomerId() == null || request.getPassword() == null) {
            throw new CustomException(CustomErrorCode.LOGIN_INPUT_INVALID);
        }

        User user = userRepository.findByCustomerId(request.getCustomerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(CustomErrorCode.INVALID_PASSWORD);
        }

        // AccessToken, RefreshToken 생성 후
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUserType().getName());
        String refreshToken = UUID.randomUUID().toString();

        // Redis 저장 (7일)
        tokenStore.saveRefreshToken(user.getUserId(), refreshToken, Duration.ofDays(7).toMillis());

        return new LoginResponse(accessToken, refreshToken);
    }

    // 토큰 재발급
    @Override
    public LoginResponse refreshAccessToken(String refreshToken) {
        // 1. 토큰 유효성 확인 : JWT 파싱 금지. UUID이므로 Redis로만 검증
        Long userId = tokenStore.findUserIdByRefresh(refreshToken);
        if (userId == null) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 사용자 조회로 role 확보
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        String newAccess = jwtTokenProvider.createAccessToken(user.getUserId(), user.getUserType().getName());
        // 회전 정책(권장): 기존 refresh 삭제 후 새로 발급
        String newRefresh = UUID.randomUUID().toString();

        tokenStore.deleteByRefresh(refreshToken);
        tokenStore.saveRefreshToken(user.getUserId(), newRefresh, Duration.ofDays(7).toMillis());

        return new LoginResponse(newAccess, newRefresh);
    }

    @Override
    public void logout(String accessToken) {
        // 1) 헤더 포맷 검증은 Controller에서 수행했다고 가정

        // 2) 토큰 유효성 & 클레임 파싱
        Claims claims = jwtTokenProvider.parseClaims(accessToken); // 만료여도 parseClaims 처리하도록 구현되어 있어야 함
        Long userId = Long.valueOf(String.valueOf(claims.getSubject())); // subject에 userId 저장한 구조

        // 3) DB 또는 캐시에서 userId로 userType 조회 (3=companyUser, 4=generalUser만 허용)
        Integer typeId = userRepository.findById(userId)
                .map(user -> user.getUserType().getTypeId().intValue()) // Long → int 변환
                .orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));

        if (!(typeId == 3 || typeId == 4)) {
            throw new CustomException(CustomErrorCode.ACCESS_DENIED); // 관리 계정(1,2)은 별도 로직 사용
        }

        // 4) AccessToken 남은 유효시간 → 블랙리스트 TTL로 사용
        long ttl = jwtTokenProvider.getRemainingValidity(accessToken); // 만료/예외 시 0을 반환하도록 구현되어 있어야 함
        if (ttl <= 0) {
            throw new CustomException(CustomErrorCode.INVALID_ACCESS_TOKEN);
        }

        // 5) 블랙리스트 등록 & RefreshToken 제거
        tokenStore.blacklistAccessToken(accessToken, ttl); // 해당 AccessToken 재사용 차단
        tokenStore.deleteRefreshToken(userId); // 해당 유저의 RefreshToken 제거
    }
}