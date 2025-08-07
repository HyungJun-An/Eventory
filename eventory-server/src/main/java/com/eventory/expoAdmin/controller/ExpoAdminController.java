package com.eventory.expoAdmin.controller;

import com.eventory.common.entity.User;
import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.RefundResponseDto;
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

    // 환불 요청 관리
    @GetMapping("/{expoId}/refund")
    public ResponseEntity<List<RefundResponseDto>> findAllRefunds(@PathVariable Long expoId) {
        List<RefundResponseDto> refundResponseDto = expoAdminService.findAllRefunds(expoId);
        return ResponseEntity.ok(refundResponseDto);
    }
}
