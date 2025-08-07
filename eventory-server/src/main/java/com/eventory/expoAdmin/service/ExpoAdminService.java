package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.*;

import java.util.List;
import java.util.Map;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos(Long expoAdminId);
    SalesResponseDto findSalesStatistics(Long expoId);
    List<Map<String, Object>> findYearlySales(Long expoId);
    List<Map<String, Object>> findMonthlySales(Long expoId);
    List<Map<String, Object>> findDailySales(Long expoId);

    DashboardResponseDto getDashboardSummary(Long expoId);

    List<ReservationStatResponseDto> getDailyReservationStats(Long expoId);
    List<ReservationStatResponseDto> getWeeklyReservationStats(Long expoId);
    List<ReservationStatResponseDto> getMonthlyReservationStats(Long expoId);
}
