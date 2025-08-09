package com.eventory.expoUser.controller;

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
}
