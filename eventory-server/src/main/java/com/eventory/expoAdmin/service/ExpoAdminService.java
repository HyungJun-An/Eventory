package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ExpoAdminService {
    List<ExpoResponseDto> findAllExpos(Long expoAdminId);
    SalesResponseDto findSalesStatistics(Long expoId);
    List<Map<String, Object>> findYearlySales(Long expoId);
    List<Map<String, Object>> findMonthlySales(Long expoId);
    List<Map<String, Object>> findDailySales(Long expoId);
    List<RefundResponseDto> findAllRefunds(Long expoId);

    List<RefundResponseDto> findRefundsByStatus(Long expoId, String status);
    List<PaymentResponseDto> findAllPayments(Long expoId, String reservationCode);

    void updateRefundStatus(Long refundId, RefundRequestDto request);

    // Resource downloadPaymentsExcel(Long expoId);

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
