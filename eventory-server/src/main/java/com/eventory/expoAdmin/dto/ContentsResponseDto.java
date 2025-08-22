package com.eventory.expoAdmin.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ContentsResponseDto(
        Long expoId,
        String title,
        String description,
        String imgURL,
        String location,
        String phone,
        String email,
        BigDecimal price,
        LocalDate startDate,
        LocalDate endDate,
        String expoAdminName,
        List<String> categories
) {}