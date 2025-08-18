package com.eventory.expoAdmin.service;

import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.expoAdmin.dto.*;
import java.time.LocalDate;
import java.util.List;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos(Long expoAdminId);

    DashboardResponseDto findDashboardSummary(Long expoAdminId, Long expoId);

    List<ReservationStatResponseDto> findDailyReservationStats(Long expoId);
    List<ReservationStatResponseDto> findWeeklyReservationStats(Long expoId);
    List<ReservationStatResponseDto> findMonthlyReservationStats(Long expoId);

    StatReportRowResponseDto buildStatDto(Long expoId, LocalDate start, LocalDate end, String label);

    List<StatReportRowResponseDto> findDailyReportData(Long expoId);
    List<StatReportRowResponseDto> findWeeklyReportData(Long expoId);
    List<StatReportRowResponseDto> findMonthlyReportData(Long expoId);

    FileDownloadDto exportCsvReport(Long expoId, String period);
    FileDownloadDto exportExcelReport(Long expoId, String period);

    List<TicketTypeRatioResponseDto> findTicketTypeRatios(Long expoId);
}
