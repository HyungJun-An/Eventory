package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.PaymentResponseDto;
import com.eventory.expoAdmin.dto.RefundRequestDto;
import com.eventory.expoAdmin.dto.RefundResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;

public interface SalesAdminService {
    SalesResponseDto findSalesStatistics(Long expoId);
    List<Map<String, Object>> findYearlySales(Long expoId);
    List<Map<String, Object>> findMonthlySales(Long expoId);
    List<Map<String, Object>> findDailySales(Long expoId);
    List<RefundResponseDto> findAllRefunds(Long expoId, String status, Integer page, Integer size);

    List<PaymentResponseDto> findAllPayments(Long expoId, String reservationCode, Integer page, Integer size);
    List<PaymentResponseDto> findAllPayments(Long expoId, String reservationCode);

    void updateRefundStatus(Long refundId, RefundRequestDto request);

    Resource downloadPaymentsExcel(List<PaymentResponseDto> paymentResponseDto);
}
