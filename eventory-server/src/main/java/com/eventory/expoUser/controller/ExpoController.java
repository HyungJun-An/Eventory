package com.eventory.expoUser.controller;

import com.eventory.expoUser.dto.ExpoDetailResponseDto;
import com.eventory.expoUser.dto.ExpoMainPageResponseDto;
import com.eventory.expoUser.service.ExpoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/expos")
@RequiredArgsConstructor
public class ExpoController {

    private final ExpoService expoService;

    @GetMapping
    public List<ExpoMainPageResponseDto> getMainPageExpos() {
        return expoService.getMainPageExpos();
    }

    // 박람회 상세 (상세 화면·결제 화면의 가격·잔여석 표시)
    @GetMapping("/{expoId}")
    public ExpoDetailResponseDto getExpoDetail(@PathVariable Long expoId) {
        return expoService.getExpoDetail(expoId);
    }
}
