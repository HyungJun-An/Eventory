package com.eventory.expoAdmin.controller;

import com.eventory.common.entity.User;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.*;
import com.eventory.expoAdmin.service.ExpoAdminService;
import com.eventory.expoAdmin.service.SalesAdminService;
import com.eventory.expoAdmin.web.FileResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ExpoAdminController {

    private final ExpoAdminService expoAdminService;
    private final SalesAdminService salesAdminService;

    // 해당 박람회 관리자에 속하는 전체 박람회 목록
    @GetMapping("/expos")
    public ResponseEntity<List<ExpoResponseDto>> findAllExpos(@AuthenticationPrincipal User user) {
        Long expoAdminId = user.getUserId();
        List<ExpoResponseDto> expos = expoAdminService.findAllExpos(expoAdminId);
        return ResponseEntity.ok(expos);
    }

    // 누적 매출, 총 결제 건수, 총 환불 건수
    @GetMapping("/expos/{expoId}/sales")
    public ResponseEntity<SalesResponseDto> findSalesStatistics(@PathVariable Long expoId) {
        SalesResponseDto salesResponseDto = salesAdminService.findSalesStatistics(expoId);
        return ResponseEntity.ok(salesResponseDto);
    }

    // 연간 매출, 월간 매출, 일주일간 매출
    @GetMapping("/expos/{expoId}/stats")
    public ResponseEntity<List<Map<String, Object>>> findSales(@PathVariable Long expoId, @RequestParam String range) {
        List<Map<String, Object>> sales = switch (range.toLowerCase()) {
            case "daily" -> salesAdminService.findDailySales(expoId);
            case "monthly" -> salesAdminService.findMonthlySales(expoId);
            case "yearly" -> salesAdminService.findYearlySales(expoId);
            default -> throw new CustomException(CustomErrorCode.NOT_FOUNT_RANGE);
        };
        return ResponseEntity.ok(sales);
    }

    // 결제 내역 관리
    @GetMapping("/expos/{expoId}/payment")
    public ResponseEntity<List<PaymentResponseDto>> findAllPayments(
            @PathVariable Long expoId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        List<PaymentResponseDto> paymentResponseDto;

        if (page!=null && size!=null) { // 페이징 O
            paymentResponseDto = salesAdminService.findAllPayments(expoId, code, page, size);
        } else { // 페이징 X
            paymentResponseDto = salesAdminService.findAllPayments(expoId, code);
        }

        return ResponseEntity.ok(paymentResponseDto);
    }

    // 결제 내역 엑셀 다운로드
    @PostMapping("/expos/{expoId}/payment/report")
    public ResponseEntity<Resource> downloadPaymentsExcel(@PathVariable Long expoId) {

        List<PaymentResponseDto> paymentResponseDto = salesAdminService.findAllPayments(expoId, null);

        Resource excel = salesAdminService.downloadPaymentsExcel(paymentResponseDto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payment.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // 환불 요청 관리, 환불 대기 관리, 환불 승인 관리
    @GetMapping("/expos/{expoId}/refund")
    public ResponseEntity<List<RefundResponseDto>> findAllRefunds(
            @PathVariable Long expoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "7") Integer size) {

        List<RefundResponseDto> refundResponseDto = salesAdminService.findAllRefunds(expoId, status, page, size);

        return ResponseEntity.ok(refundResponseDto);
    }

    // 환불 상태 변경
    @PatchMapping("/refund/{refundId}/status")
    public ResponseEntity<Void> updateRefundStatus(@PathVariable Long refundId, @RequestBody RefundRequestDto request) {
        salesAdminService.updateRefundStatus(refundId, request);
        return ResponseEntity.ok().build();
    }

    // 대시보드 카드 조회
    @GetMapping("/expos/{expoId}/dashboard/summary")
    public ResponseEntity<DashboardResponseDto> findDashboardSummary(@PathVariable Long expoId) {
        DashboardResponseDto summary = expoAdminService.findDashboardSummary(expoId);
        return ResponseEntity.ok(summary);
    }

    // 일별, 주별, 월별 예약 수 (막대그래프)
    @GetMapping("/expos/{expoId}/dashboard/stats")
    public ResponseEntity<List<ReservationStatResponseDto>> getReservationStats(@PathVariable Long expoId,
                                                                     @RequestParam String period // "daily" | "weekly" | "monthly"
                                                                     ) {
        return switch (period.toLowerCase()) {
            case "daily" -> ResponseEntity.ok(expoAdminService.findDailyReservationStats(expoId)); // 일별 예약 수 (최근 7일간 일별 예약 수 (오늘 기준 지난 7일(6일 전 ~ 오늘)))
            case "weekly" -> ResponseEntity.ok(expoAdminService.findWeeklyReservationStats(expoId)); // 주별 예약 수 (최근 4주간 주차별 예약 수 (오늘 기준 최근 4주 (주 단위 구간)))
            case "monthly" -> ResponseEntity.ok(expoAdminService.findMonthlyReservationStats(expoId)); // 월별 예약 수 (최근 4개월 간 월별 예약 수 (오늘 기준 최근 4개월 (월 단위))
            default -> throw new CustomException(CustomErrorCode.INVALID_PERIOD);
        };
    }

    // 통계 리포트 .csv 형식으로 다운로드
    @GetMapping(
            value = "/expos/{expoId}/dashboard/{period}/csv",
            produces = "text/csv"
    )
    public ResponseEntity<Resource> downloadCsv(@PathVariable Long expoId,
                                                @PathVariable String period) {
        return FileResponseUtils.toDownloadResponse(
                expoAdminService.exportCsvReport(expoId, period), // byte[] + 파일명 + contentType
                "text/csv; charset=UTF-8"                         // CSV는 명시적으로 강제
        );
    }

    // 통계 리포트 .xlsx 형식으로 다운로드
    @GetMapping(
            value = "/expos/{expoId}/dashboard/{period}/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<Resource> downloadExcel(@PathVariable Long expoId,
                                                  @PathVariable String period) {
        return FileResponseUtils.toDownloadResponse(
                expoAdminService.exportExcelReport(expoId, period)
        );
    }

    // 티켓 종류별(무료/유료) 예약 비율 (파이차트)
    @GetMapping("/expos/{expoId}/dashboard/ticket-types")
    public ResponseEntity<List<TicketTypeRatioResponseDto>> getTicketTypeRatios(@PathVariable Long expoId) {
        List<TicketTypeRatioResponseDto> ratios = expoAdminService.findTicketTypeRatios(expoId);
        return ResponseEntity.ok(ratios);
    }

}
