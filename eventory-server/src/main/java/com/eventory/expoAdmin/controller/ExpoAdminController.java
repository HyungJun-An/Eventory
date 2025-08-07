package com.eventory.expoAdmin.controller;

import com.eventory.common.entity.User;
import com.eventory.expoAdmin.dto.DashboardResponseDto;
import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.ReservationStatResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.expoAdmin.service.ExpoAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 연간 매출
    @GetMapping("/{expoId}/yearly")
    public ResponseEntity<List<Map<String, Object>>> findYearlySales(@PathVariable Long expoId) {
        List<Map<String, Object>> yearlySales = expoAdminService.findYearlySales(expoId);
        return ResponseEntity.ok(yearlySales);
    }

    // 월간 매출
    @GetMapping("/{expoId}/monthly")
    public ResponseEntity<List<Map<String, Object>>> findMonthlySales(@PathVariable Long expoId) {
        List<Map<String, Object>> monthlySales = expoAdminService.findMonthlySales(expoId);
        return ResponseEntity.ok(monthlySales);
    }

    // 일주일간 매출
    @GetMapping("/{expoId}/daily")
    public ResponseEntity<List<Map<String, Object>>> findDailySales(@PathVariable Long expoId) {
        List<Map<String, Object>> dailySales = expoAdminService.findDailySales(expoId);
        return ResponseEntity.ok(dailySales);
    }

    // 대시보드 카드 조회
    @GetMapping("/expos/{expoId}/dashboard/summary")
    public ResponseEntity<DashboardResponseDto> getDashboardSummary(@PathVariable Long expoId) {
        DashboardResponseDto summary = expoAdminService.getDashboardSummary(expoId);
        return ResponseEntity.ok(summary);
    }

    // 일별 예약 수 (이번 주 월~일 요일별 예약 수 (오늘이 포함된 월~일까지 7일간))
    @GetMapping("/expos/{expoId}/dashboard/daily")
    public ResponseEntity<List<ReservationStatResponseDto>> getDailyReservationStats(@PathVariable Long expoId) {
        List<ReservationStatResponseDto>  dailyReservationStats = expoAdminService.getDailyReservationStats(expoId);
        return ResponseEntity.ok(dailyReservationStats);
    }

    // 주별 예약 수 (최근 4주간 주차별 예약 수 (오늘 기준 최근 4주 (주 단위 구간)))
    @GetMapping("/expos/{expoId}/dashboard/weekly")
    public ResponseEntity<List<ReservationStatResponseDto>> getWeeklyReservationStats(@PathVariable Long expoId) {
        List<ReservationStatResponseDto> weeklyReservationStats = expoAdminService.getWeeklyReservationStats(expoId);
        return ResponseEntity.ok(weeklyReservationStats);
    }

    // 월별 예약 수 (최근 4개월 간 월별 예약 수 (오늘 기준 최근 4개월 (월 단위))
    @GetMapping("/expos/{expoId}/dashboard/monthly")
    public ResponseEntity<List<ReservationStatResponseDto>> getMonthlyReservationStats(@PathVariable Long expoId) {
        List<ReservationStatResponseDto> monthlyReservationStats = expoAdminService.getMonthlyReservationStats(expoId);
        return ResponseEntity.ok(monthlyReservationStats);
    }
}
