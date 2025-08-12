package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import org.springframework.core.io.Resource;
import java.util.List;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos(Long expoAdminId);

    DashboardResponseDto getDashboardSummary(Long expoId);

    List<ReservationStatResponseDto> getDailyReservationStats(Long expoId);
    List<ReservationStatResponseDto> getWeeklyReservationStats(Long expoId);
    List<ReservationStatResponseDto> getMonthlyReservationStats(Long expoId);

    StatReportRowResponseDto buildStatDto(Long expoId, LocalDate start, LocalDate end, String label);

    List<StatReportRowResponseDto> getDailyReportData(Long expoId);
    List<StatReportRowResponseDto> getWeeklyReportData(Long expoId);
    List<StatReportRowResponseDto> getMonthlyReportData(Long expoId);

    FileDownloadDto exportCsvReport(Long expoId, String period);
    FileDownloadDto exportExcelReport(Long expoId, String period);

    List<TicketTypeRatioResponseDto> getTicketTypeRatios(Long expoId);
}
