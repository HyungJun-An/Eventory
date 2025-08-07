package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.expoAdmin.dto.YearlySalesResponseDto;

import java.util.List;
import java.util.Map;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos(Long expoAdminId);
    SalesResponseDto findSalesStatistics(Long expoId);
    List<Map<String, Object>> findYearlySales(Long expoId);
    List<Map<String, Object>> findMonthlySales(Long expoId);
    List<Map<String, Object>> findDailySales(Long expoId);
}
