package com.eventory.expoAdmin.controller;

import com.eventory.common.entity.User;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.RefundResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.expoAdmin.service.ExpoAdminService;
import lombok.RequiredArgsConstructor;
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
}
