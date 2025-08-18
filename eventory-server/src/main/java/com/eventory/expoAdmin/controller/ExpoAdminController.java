package com.eventory.expoAdmin.controller;

import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.*;
import com.eventory.expoAdmin.service.BoothService;
import com.eventory.expoAdmin.service.ExpoAdminService;
import com.eventory.expoAdmin.service.ExpoInfoService;
import com.eventory.expoAdmin.service.SalesAdminService;
import com.eventory.expoAdmin.web.FileResponseUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ExpoAdminController {

    private final ExpoAdminService expoAdminService;
    private final SalesAdminService salesAdminService;
    private final ExpoInfoService expoInfoService;
    private final BoothService boothService;

    // 박람회 신청
    @PostMapping("/expos")
    public ResponseEntity<Void> createExpo(@Valid @RequestBody ExpoCreateRequestDto requestDto) {
        expoAdminService.createExpo(requestDto);
        return ResponseEntity.ok().build();
    }

    // 해당 박람회 관리자에 속하는 전체 박람회 목록
    @GetMapping("/expos")
    public ResponseEntity<List<ExpoResponseDto>> findAllExpos(@AuthenticationPrincipal CustomUserPrincipal expoAdmin) {
        Long expoAdminId = expoAdmin.getId();
        List<ExpoResponseDto> expos = expoAdminService.findAllExpos(expoAdminId);
        return ResponseEntity.ok(expos);
    }

    // 누적 매출, 총 결제 건수, 총 환불 건수
    @GetMapping("/expos/{expoId}/sales")
    public ResponseEntity<SalesResponseDto> findSalesStatistics(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId) {
        Long expoAdminId = expoAdmin.getId();
        SalesResponseDto salesResponseDto = salesAdminService.findSalesStatistics(expoAdminId, expoId);
        return ResponseEntity.ok(salesResponseDto);
    }

    // 연간 매출, 월간 매출, 일주일간 매출
    @GetMapping("/expos/{expoId}/stats")
    public ResponseEntity<List<Map<String, Object>>> findSales(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId, @RequestParam String range) {
        Long expoAdminId = expoAdmin.getId();
        List<Map<String, Object>> sales = switch (range.toLowerCase()) {
            case "daily" -> salesAdminService.findDailySales(expoAdminId, expoId);
            case "monthly" -> salesAdminService.findMonthlySales(expoAdminId, expoId);
            case "yearly" -> salesAdminService.findYearlySales(expoAdminId, expoId);
            default -> throw new CustomException(CustomErrorCode.NOT_FOUNT_RANGE);
        };
        return ResponseEntity.ok(sales);
    }

    // 결제 내역 관리
    @GetMapping("/expos/{expoId}/payment")
    public ResponseEntity<List<PaymentResponseDto>> findAllPayments(
            @AuthenticationPrincipal CustomUserPrincipal expoAdmin,
            @PathVariable Long expoId,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        Long expoAdminId = expoAdmin.getId();

        List<PaymentResponseDto> paymentResponseDto;

        if (page!=null && size!=null) { // 페이징 O
            paymentResponseDto = salesAdminService.findAllPayments(expoAdminId, expoId, code, startDate, endDate, page, size);
        } else { // 페이징 X
            paymentResponseDto = salesAdminService.findAllPayments(expoAdminId, expoId);
        }

        return ResponseEntity.ok(paymentResponseDto);
    }

    // 결제 내역 엑셀 다운로드
    @PostMapping("/expos/{expoId}/payment/report")
    public ResponseEntity<Resource> downloadPaymentsExcel(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId) {

        Long expoAdminId = expoAdmin.getId();

        List<PaymentResponseDto> paymentResponseDto = salesAdminService.findAllPayments(expoAdminId, expoId);

        Resource excel = salesAdminService.downloadPaymentsExcel(paymentResponseDto);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payment.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    // 환불 요청 관리, 환불 대기 관리, 환불 승인 관리
    @GetMapping("/expos/{expoId}/refund")
    public ResponseEntity<List<RefundResponseDto>> findAllRefunds(
            @AuthenticationPrincipal CustomUserPrincipal expoAdmin,
            @PathVariable Long expoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "7") Integer size) {

        Long expoAdminId = expoAdmin.getId();

        List<RefundResponseDto> refundResponseDto = salesAdminService.findAllRefunds(expoAdminId, expoId, status, page, size);

        return ResponseEntity.ok(refundResponseDto);
    }

    // 환불 상태 변경
    @PatchMapping("/refund/{refundId}/status")
    public ResponseEntity<Void> updateRefundStatus(@PathVariable Long refundId, @Valid @RequestBody RefundRequestDto request) {
        salesAdminService.updateRefundStatus(refundId, request);
        return ResponseEntity.ok().build();
    }

    // 대시보드 카드 조회
    @GetMapping("/expos/{expoId}/dashboard/summary")
    public ResponseEntity<DashboardResponseDto> findDashboardSummary(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId) {
        Long expoAdminId = expoAdmin.getId();
        DashboardResponseDto summary = expoAdminService.findDashboardSummary(expoAdminId, expoId);
        return ResponseEntity.ok(summary);
    }

    // 일별, 주별, 월별 예약 수 (막대그래프)
    @GetMapping("/expos/{expoId}/dashboard/stats")
    public ResponseEntity<List<ReservationStatResponseDto>> getReservationStats(
            @AuthenticationPrincipal CustomUserPrincipal expoAdmin,
            @PathVariable Long expoId,
            @RequestParam String period // "daily" | "weekly" | "monthly"
    ) {
        Long expoAdminId = expoAdmin.getId();
        return switch (period.toLowerCase()) {
            case "daily" -> ResponseEntity.ok(expoAdminService.findDailyReservationStats(expoAdminId, expoId)); // 일별 예약 수 (최근 7일간 일별 예약 수 (오늘 기준 지난 7일(6일 전 ~ 오늘)))
            case "weekly" -> ResponseEntity.ok(expoAdminService.findWeeklyReservationStats(expoAdminId, expoId)); // 주별 예약 수 (최근 4주간 주차별 예약 수 (오늘 기준 최근 4주 (주 단위 구간)))
            case "monthly" -> ResponseEntity.ok(expoAdminService.findMonthlyReservationStats(expoAdminId, expoId)); // 월별 예약 수 (최근 4개월 간 월별 예약 수 (오늘 기준 최근 4개월 (월 단위))
            default -> throw new CustomException(CustomErrorCode.INVALID_PERIOD);
        };
    }

    // 통계 리포트 .csv 형식으로 다운로드
    @GetMapping(
            value = "/expos/{expoId}/dashboard/{period}/csv",
            produces = "text/csv"
    )
    public ResponseEntity<Resource> downloadCsv(@AuthenticationPrincipal CustomUserPrincipal expoAdmin,
                                                @PathVariable Long expoId,
                                                @PathVariable String period) {
        Long expoAdminId = expoAdmin.getId();
        return FileResponseUtils.toDownloadResponse(
                expoAdminService.exportCsvReport(expoAdminId, expoId, period), // byte[] + 파일명 + contentType
                "text/csv; charset=UTF-8"                         // CSV는 명시적으로 강제
        );
    }

    // 통계 리포트 .xlsx 형식으로 다운로드
    @GetMapping(
            value = "/expos/{expoId}/dashboard/{period}/excel",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<Resource> downloadExcel(@AuthenticationPrincipal CustomUserPrincipal expoAdmin,
                                                  @PathVariable Long expoId,
                                                  @PathVariable String period) {
        Long expoAdminId = expoAdmin.getId();
        return FileResponseUtils.toDownloadResponse(
                expoAdminService.exportExcelReport(expoAdminId, expoId, period)
        );
    }

    // 티켓 종류별(무료/유료) 예약 비율 (파이차트)
    @GetMapping("/expos/{expoId}/dashboard/ticket-types")
    public ResponseEntity<List<TicketTypeRatioResponseDto>> getTicketTypeRatios(@AuthenticationPrincipal CustomUserPrincipal expoAdmin,  @PathVariable Long expoId) {
        Long expoAdminId = expoAdmin.getId();
        List<TicketTypeRatioResponseDto> ratios = expoAdminService.findTicketTypeRatios(expoAdminId, expoId);
        return ResponseEntity.ok(ratios);
    }

    // 박람회 담당자 정보 조회
    @GetMapping("/profile")
    public ResponseEntity<ManagerResponseDto> findExpoManagerInfo(@AuthenticationPrincipal CustomUserPrincipal expoAdmin) {
        Long expoAdminId = expoAdmin.getId();
        ManagerResponseDto responseDto = expoInfoService.findExpoManagerInfo(expoAdminId);
        return ResponseEntity.ok(responseDto);
    }

    // 박람회 담당자 정보 수정
    @PutMapping("/profile")
    public ResponseEntity<Void> updateExpoManagerInfo(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @Valid @RequestBody ManagerRequestDto requestDto) {
        Long expoAdminId = expoAdmin.getId();
        expoInfoService.updateExpoManagerInfo(expoAdminId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 특정 박람회 정보 조회
    @GetMapping("/expos/{expoId}")
    public ResponseEntity<ExpoResponseDto> findExpoInfo(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId) {
        Long expoAdminId = expoAdmin.getId();
        ExpoResponseDto responseDto = expoInfoService.findExpoInfo(expoAdminId, expoId);
        return ResponseEntity.ok(responseDto);
    }

    // 특정 박람회 정보 수정
    @PutMapping("/expos/{expoId}")
    public ResponseEntity<Void> updateExpoInfo(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId, @Valid @RequestBody ExpoUpdateRequestDto requestDto) {
        Long expoAdminId = expoAdmin.getId();
        expoInfoService.updateExpoInfo(expoAdminId, expoId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 특정 박람회 배너 신청
    @PostMapping("/expos/{expoId}/banner")
    public ResponseEntity<Void> createExpoBanner(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId, @Valid @RequestBody BannerCreateRequestDto requestDto) {
        Long expoAdminId = expoAdmin.getId();
        expoInfoService.createExpoBanner(expoAdminId, expoId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 특정 박람회 배너 조회
    @GetMapping("/expos/{expoId}/banner")
    public ResponseEntity<BannerResponseDto> findExpoBanner(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId) {
        Long expoAdminId = expoAdmin.getId();
        BannerResponseDto responseDto = expoInfoService.findExpoBanner(expoAdminId, expoId);
        return ResponseEntity.ok(responseDto);
    }

    // 특정 박람회 배너 수정
    @PutMapping("/expos/{expoId}/banner")
    public ResponseEntity<Void> updateExpoBanner(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId, @Valid @RequestBody BannerUpdateRequestDto requestDto) {
        Long expoAdminId = expoAdmin.getId();
        expoInfoService.updateExpoBanner(expoAdminId, expoId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 특정 박람회에 대한 부스 신청 목록 조회
    @GetMapping("/expos/{expoId}/booths")
    public ResponseEntity<List<BoothResponseDto>> findAllBooths(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId) {
        Long expoAdminId = expoAdmin.getId();
        List<BoothResponseDto> booths = boothService.findAllBooths(expoAdminId, expoId);
        return ResponseEntity.ok(booths);
    }

    // 특정 박람회에 대한 특정 부스 상태 변경
    @PutMapping("/expos/{expoId}/booths/{boothId}")
    public ResponseEntity<Void> updateBooth(@AuthenticationPrincipal CustomUserPrincipal expoAdmin, @PathVariable Long expoId, @PathVariable Long boothId, @Valid @RequestBody BoothRequestDto requestDto) {
        Long expoAdminId = expoAdmin.getId();
        boothService.updateBooth(expoAdminId, expoId, boothId, requestDto);
        return ResponseEntity.ok().build();
    }
}