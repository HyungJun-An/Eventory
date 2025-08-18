package com.eventory.companyUser.controller;

import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.companyUser.dto.BoothRequestDto;
import com.eventory.companyUser.dto.BoothResponseDto;
import com.eventory.companyUser.dto.UserRequestDto;
import com.eventory.companyUser.dto.UserResponseDto;
import com.eventory.companyUser.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class companyController {

    private final CompanyService companyService;

    // 부스 신청
    @PostMapping("/auth/booth")
    public ResponseEntity<Void> createBooth(@AuthenticationPrincipal CustomUserPrincipal companyUser, @Valid @RequestBody BoothRequestDto requestDto) {
        Long companyUserId = companyUser.getId();
        companyService.createBooth(companyUserId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 내 정보 조회
    @GetMapping("/auth/profile")
    public ResponseEntity<UserResponseDto> findProfile(@AuthenticationPrincipal CustomUserPrincipal companyUser) {
        Long companyUserId = companyUser.getId();
        UserResponseDto responseDto = companyService.findProfile(companyUserId);
        return ResponseEntity.ok(responseDto);
    }

    // 내 정보 수정
    @PutMapping("/auth/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal CustomUserPrincipal companyUser, @Valid @RequestBody UserRequestDto requestDto) {
        Long companyUserId = companyUser.getId();
        companyService.updateProfile(companyUserId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 부스 목록 조회
    @GetMapping("/user/booths")
    public ResponseEntity<List<BoothResponseDto>> findAllBooths(@AuthenticationPrincipal CustomUserPrincipal companyUser) {
        Long companyUserId = companyUser.getId();
        List<BoothResponseDto> responseDto = companyService.findAllBooths(companyUserId);
        return ResponseEntity.ok(responseDto);
    }

    // 부스 상세 조회
    @GetMapping("/user/booths/{boothId}")
    public ResponseEntity<BoothResponseDto> findBooth(@AuthenticationPrincipal CustomUserPrincipal companyUser, @PathVariable Long boothId) {
        Long companyUserId = companyUser.getId();
        BoothResponseDto responseDto = companyService.findBooth(companyUserId, boothId);
        return ResponseEntity.ok(responseDto);
    }

    // 특정 부스 정보 수정
    @PutMapping("/user/booths/{boothId}")
    public ResponseEntity<Void> updateBooth(@AuthenticationPrincipal CustomUserPrincipal companyUser, @PathVariable Long boothId, @Valid @RequestBody BoothRequestDto requestDto) {
        Long companyUserId = companyUser.getId();
        companyService.updateBooth(companyUserId, boothId, requestDto);
        return ResponseEntity.ok().build();
    }
}
