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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

//    private final RedisTemplate<String, String> redisTemplate;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Override
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByCustomerId(request.getCustomerId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }
        // 1-시스템 관리자 2-박람회 관리자 3-참가업체 4-참관객
        if (request.getTypeId() == 4L) {
            if (request.getBirth() == null || request.getGender() == null || request.getPhone() == null) {
                throw new IllegalArgumentException("필수 항목이 누락되었습니다. (참관객)");
            }
        } else if (request.getTypeId() == 3L) {
            if (request.getCompanyNameKr() == null || request.getCompanyNameEng() == null ||
                    request.getCeoNameKr() == null || request.getCeoNameEng() == null ||
                    request.getCompanyAddress() == null || request.getRegistrationNum() == null) {
                throw new IllegalArgumentException("필수 항목이 누락되었습니다. (참가업체)");
            }
        } else {
            throw new IllegalArgumentException("지원하지 않는 사용자 유형입니다.");
        }

        UserType userType = userTypeRepository.findById(request.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자 유형입니다."));

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

//        String accessToken = jwtTokenProvider.createAccessToken(user.getUserId());
//        String refreshToken = jwtTokenProvider.createRefreshToken();
//
//        redisTemplate.opsForValue()
//                .set("refresh:" + user.getUserId(), refreshToken, Duration.ofDays(7));

//        return new LoginResponse(accessToken, refreshToken);
        return new LoginResponse("","");
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
    }
}