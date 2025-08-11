package com.eventory.auth.service;

import com.eventory.auth.dto.LoginRequest;
import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.dto.SignupRequest;
import com.eventory.auth.dto.SignupResponse;
import com.eventory.auth.repository.UserRepository;
import com.eventory.auth.repository.UserTypeRepository;

import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.common.entity.User;
import com.eventory.common.entity.UserType;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

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
        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken();

        // Redis 저장
        redisTemplate.opsForValue()
                .set("refresh:" + user.getUserId(), refreshToken, Duration.ofDays(7));

        return new LoginResponse(accessToken, refreshToken);
    }

    // 토큰 재발급
    @Override
    public LoginResponse refreshAccessToken(String refreshToken) {
        // 1. 토큰 유효성 확인
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(CustomErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. userId 추출
        Long userId = Long.parseLong(jwtTokenProvider.getSubject(refreshToken));

        // 3. Redis에 저장된 RefreshToken 확인
        String redisKey = "refresh:" + userId;
        String savedToken = redisTemplate.opsForValue().get(redisKey);

        // Redis에 저장된 refresh:{userId}가 있을 때만 재발급 허용
        if (!refreshToken.equals(savedToken)) {
            throw new CustomException(CustomErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 4. 새로운 AccessToken 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(userId);
        return new LoginResponse(newAccessToken, refreshToken); // RefreshToken은 그대로 전달
    }

    @Override
    public void logout(String accessToken) {
//        Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
//
//        long expiration = jwtTokenProvider.getExpiration(accessToken);
//        redisTemplate.opsForValue()
//                .set("blacklist:" + accessToken, "logout", Duration.ofMillis(expiration));
//
//        redisTemplate.delete("refresh:" + userId);
        // 1. AccessToken 유효성 확인
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new CustomException(CustomErrorCode.INVALID_ACCESS_TOKEN);
        }

        // 2. AccessToken 남은 유효 시간 계산
        long expiration = jwtTokenProvider.getRemainingValidity(accessToken);

        // 3. 블랙리스트 저장
        redisTemplate.opsForValue()
                .set("blacklist:" + accessToken, "logout", Duration.ofMillis(expiration));

        // 4. RefreshToken 삭제
        long userId = Long.parseLong(jwtTokenProvider.getSubject(accessToken));
        redisTemplate.delete("refresh:" + userId);
    }
}