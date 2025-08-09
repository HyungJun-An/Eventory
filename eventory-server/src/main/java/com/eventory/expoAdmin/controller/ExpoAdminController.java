package com.eventory.expoAdmin.controller;

import com.eventory.common.entity.User;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.DashboardResponseDto;
import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.RefundResponseDto;
import com.eventory.expoAdmin.dto.ReservationStatResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.expoAdmin.service.ExpoAdminService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ExpoAdminController {

    private final ExpoAdminService expoAdminService;

    // 해당 박람회 관리자에 속하는 전체 박람회 목록
    @GetMapping("/expos")
    public ResponseEntity<List<ExpoResponseDto>> findAllExpos(@AuthenticationPrincipal User user) {
        Long expoAdminId = user.getUserId();
        List<ExpoResponseDto> expos = expoAdminService.findAllExpos(expoAdminId);
        return ResponseEntity.ok(expos);
    }

    // 누적 매출, 총 결제 건수, 총 환불 건수
    @GetMapping("/{expoId}/sales")
    public ResponseEntity<SalesResponseDto> findSalesStatistics(@PathVariable Long expoId) {
        SalesResponseDto salesResponseDto = expoAdminService.findSalesStatistics(expoId);
        return ResponseEntity.ok(salesResponseDto);
    }

    // 연간 매출, 월간 매출, 일주일간 매출
    @GetMapping("/{expoId}/stats")
    public ResponseEntity<List<Map<String, Object>>> findSales(@PathVariable Long expoId, @RequestParam String range) {
        List<Map<String, Object>> sales;
        switch (range.toLowerCase()) {
            case "daily":
                sales = expoAdminService.findDailySales(expoId);
                break;
            case "monthly":
                sales = expoAdminService.findMonthlySales(expoId);
                break;
            case "yearly":
                sales = expoAdminService.findYearlySales(expoId);
                break;
            default:
                throw new CustomException(CustomErrorCode.NOT_FOUNT_RANGE);
        }
        return ResponseEntity.ok(sales);
    }



    // 환불 요청 관리, 환불 대기 관리, 환불 승인 관리
    @GetMapping("/{expoId}/refund")
    public ResponseEntity<List<RefundResponseDto>> findAllRefunds(@PathVariable Long expoId, @RequestParam(required = false) String status) {
        List<RefundResponseDto> refundResponseDto;
        if (status == null) {
            refundResponseDto = expoAdminService.findAllRefunds(expoId);
        } else {
            refundResponseDto = expoAdminService.findRefundsByStatus(expoId, status);
        }
        return ResponseEntity.ok(refundResponseDto);
    }

    // 대시보드 카드 조회
    @GetMapping("/expos/{expoId}/dashboard/summary")
    public ResponseEntity<DashboardResponseDto> getDashboardSummary(@PathVariable Long expoId) {
        DashboardResponseDto summary = expoAdminService.getDashboardSummary(expoId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/expos/{expoId}/dashboard/stats")
    public ResponseEntity<List<ReservationStatResponseDto>> getReservationStats(@PathVariable Long expoId,
                                                                     @RequestParam String period // "daily" | "weekly" | "monthly"
                                                                     ) {
        return switch (period.toLowerCase()) {
            case "daily" -> ResponseEntity.ok(expoAdminService.getDailyReservationStats(expoId)); // 일별 예약 수 (이번 주 월~일 요일별 예약 수 (오늘이 포함된 월~일까지 7일간))
            case "weekly" -> ResponseEntity.ok(expoAdminService.getWeeklyReservationStats(expoId)); // 주별 예약 수 (최근 4주간 주차별 예약 수 (오늘 기준 최근 4주 (주 단위 구간)))
            case "monthly" -> ResponseEntity.ok(expoAdminService.getMonthlyReservationStats(expoId)); // 월별 예약 수 (최근 4개월 간 월별 예약 수 (오늘 기준 최근 4개월 (월 단위))
            default -> throw new CustomException(CustomErrorCode.INVALID_PERIOD);
        };
    }

    // 통계 리포트 .csv 형식으로 다운로드
    @GetMapping("/expos/{expoId}/dashboard/{period}/csv")
    public ResponseEntity<Void> downloadCsv(@PathVariable Long expoId,
                            @PathVariable String period,
                            HttpServletResponse response) throws IOException {
        expoAdminService.exportCsvReport(expoId, period, response);
        return ResponseEntity.ok().build();
    }

    // 통계 리포트 .xlsx 형식으로 다운로드
    @GetMapping("/expos/{expoId}/dashboard/{period}/excel")
    public ResponseEntity<Void> downloadExcel(@PathVariable Long expoId,
                              @PathVariable String period,
                              HttpServletResponse response) throws IOException {
        expoAdminService.exportExcelReport(expoId, period, response);
        return ResponseEntity.ok().build();
    }

}
