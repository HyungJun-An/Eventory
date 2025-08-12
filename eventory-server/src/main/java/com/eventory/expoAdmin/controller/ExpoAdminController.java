package com.eventory.expoAdmin.controller;

import com.eventory.common.entity.User;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.*;
import com.eventory.expoAdmin.service.ExpoAdminService;
import com.eventory.expoAdmin.service.SalesAdminService;
import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
}
