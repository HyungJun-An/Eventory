package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos(Long expoAdminId);

    DashboardResponseDto findDashboardSummary(Long expoAdminId, Long expoId);

    List<ReservationStatResponseDto> findDailyReservationStats(Long expoAdminId, Long expoId);
    List<ReservationStatResponseDto> findWeeklyReservationStats(Long expoAdminId, Long expoId);
    List<ReservationStatResponseDto> findMonthlyReservationStats(Long expoAdminId, Long expoId);

    StatReportRowResponseDto buildStatDto(Long expoAdminId, Long expoId, LocalDate start, LocalDate end, String label);

    List<StatReportRowResponseDto> findDailyReportData(Long expoAdminId, Long expoId);
    List<StatReportRowResponseDto> findWeeklyReportData(Long expoAdminId, Long expoId);
    List<StatReportRowResponseDto> findMonthlyReportData(Long expoAdminId, Long expoId);

    FileDownloadDto exportCsvReport(Long expoAdminId, Long expoId, String period);
    FileDownloadDto exportExcelReport(Long expoAdminId, Long expoId, String period);

    List<TicketTypeRatioResponseDto> findTicketTypeRatios(Long expoAdminId, Long expoId);

    void createExpo(@Valid ExpoCreateRequestDto requestDto);
}
