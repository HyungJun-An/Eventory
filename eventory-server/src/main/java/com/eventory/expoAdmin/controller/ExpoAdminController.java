package com.eventory.expoAdmin.controller;

import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.service.ExpoAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ExpoAdminController {

    private final ExpoAdminService expoAdminService;

    // 해당 박람회 관리자에 속하는 전체 박람회 목록
    /*public ResponseEntity<List<ExpoResponseDto>> findAllExpos(@AuthenticationPrincipal User user) {
        Long expoAdminId = user.getUserId;
        List<ExpoResponseDto> expos = expoAdminService.findAllExpos(expoAdminId);
        return ResponseEntity.ok(expos);
    }*/


}
