package com.eventory.expoAdmin.service;

import com.eventory.common.entity.*;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.*;
import com.eventory.common.repository.*;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpoAdminServiceImpl implements ExpoAdminService {

    private final ExpoAdminRepository expoAdminRepository;
    private final ExpoRepository expoRepository;
    private final ExpoStatisticsRepository expoStatisticsRepository;
    private final RefundRepository refundRepository;
    private final ReservationRepository reservationRepository;
    private final ExpoMapper expoMapper;

    // 해당 박람회 관리자에 속하는 전체 박람회 목록
    @Override
    public List<ExpoResponseDto> findAllExpos(Long expoAdminId) {

        // 연관관계 ExpoAdmin의 기본키(expoAdminId)로 Expo 조회 및 제목(title) 오름차순 정렬
        List<Expo> expos = expoRepository.findByExpoAdmin_ExpoAdminIdOrderByTitleAsc(expoAdminId);

        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return expos.stream()
                .map(expoMapper::toExpoResponseDto)
                .collect(Collectors.toList());
    }

    // 누적 매출, 총 결제 건수, 총 환불 건수
    @Override
    public SalesResponseDto findSalesStatistics(Long expoId) {

        // 기본키(expoId)로 ExpoStatistics 조회
        ExpoStatistics expoStatistics = expoStatisticsRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUNT_STATISTICS));

        // 특정 박람회(expoId)에 해당하는 환불 데이터 개수 조회
        long refundCount = refundRepository.countRefundsByExpoId(expoId);

        // dto객체로 변환 및 반환
        return expoMapper.toSalesResponseDto(expoId, expoStatistics, refundCount);
    }

    // 연간 매출
    @Override
    public List<Map<String, Object>> findYearlySales(Long expoId) {

        // 특정 박람회(expoId)의 연도별 매출 합계 조회
        List<Object[]> yearlySales = reservationRepository.findYearlySalesByExpoId(expoId);

        // 스트림 각 요소를 List<Map<String, Object>>로 변환 및 반환
        return yearlySales.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("year", row[0]);
            map.put("totalAmount", row[1]);
            return map;
        }).collect(Collectors.toList());
    }

    // 월간 매출
    @Override
    public List<Map<String, Object>> findMonthlySales(Long expoId) {

        // 현재 연도 조회
        int currentYear = Year.now().getValue();

        // 특정 박람회(expoId)의 특정 연도의 월별 매출 합계 조회
        List<Object[]> monthlySales = reservationRepository.findMonthySalesByExpoId(expoId, currentYear);

        // 스트림 각 요소를 List<Map<String, Object>>로 변환 및 반환
        return monthlySales.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("month", row[0]);
            map.put("totalAmount", row[1]);
            return map;
        }).collect(Collectors.toList());
    }

    // 지난 일주일간 매출
    @Override
    public List<Map<String, Object>> findDailySales(Long expoId) {

        // 오늘 날짜 조회
        LocalDate today = LocalDate.now();

        // 오늘 포함 최근 7일
        LocalDateTime start = today.minusDays(6).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        // 특정 박람회(expoId)의 최근 7일 일별 매출 합계 조회
        List<Object[]> result = reservationRepository.findDailySalesLast7Days(expoId, start, end);

        // 스트림 각 요소 Map으로 변환
        Map<String, Long> salesMap = result.stream().collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> ((Number) row[1]).longValue()
        ));

        // 매출 없는 null값을 0으로 교체
        List<Map<String, Object>> dailySales = new ArrayList<>();
        for (int i=0; i<7; i++) {
            LocalDate date = today.minusDays(6-i);
            String dateStr = date.toString();

            Map<String, Object> map = new HashMap<>();
            map.put("date", dateStr);
            map.put("totalAmount", salesMap.getOrDefault(dateStr, 0L));

            dailySales.add(map);
        }

        return dailySales;
    }

    // 결제 내역 관리
    @Override
    public List<PaymentResponseDto> findAllPayments(Long expoId, String code) {

        // 특정 박람회(expoId)에 해당하는 예약 조회
        List<Reservation> reservations = reservationRepository.findByExpoIdAndReservationCode(expoId, code);

        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return reservations.stream()
                .map(expoMapper::toPaymentResponseDto)
                .collect(Collectors.toList());
    }

    // 환불 요청 관리
    @Override
    public List<RefundResponseDto> findAllRefunds(Long expoId) {

        // 특정 박람회(expoId)에 해당하는 결제 조회
        List<Long> paymentIds = reservationRepository.findPaymentIdsByExpoId(expoId);

        // 비어있으면 빈 리스트 반환
        if(paymentIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 특정 박람회(expoId)에 해당하는 환불 조회
        List<Refund> refunds = refundRepository.findByPayment_PaymentIdIn(paymentIds);

        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return refunds.stream()
                .map(expoMapper::toRefundResponseDto)
                .collect(Collectors.toList());
    }

    // 환불 대기, 환불 완료
    @Override
    public List<RefundResponseDto> findRefundsByStatus(Long expoId, String status) {

        // 특정 박람회(expoId)에 해당하는 결제 조회
        List<Long> paymentIds = reservationRepository.findPaymentIdsByExpoId(expoId);

        // 비어있으면 빈 리스트 반환
        if(paymentIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 특정 박람회(expoId)에 해당하는 환불 조회
        List<Refund> refunds = refundRepository.findByPayment_PaymentIdIn(paymentIds);

        RefundStatus targetStatus;
        try {
            targetStatus = RefundStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Collections.emptyList();
        }

        List<Refund> filteredRefunds = refunds.stream()
                .filter(refund -> refund.getStatus() == targetStatus).toList();

        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return filteredRefunds.stream()
                .map(expoMapper::toRefundResponseDto)
                .collect(Collectors.toList());
    }

    // 환불 상태 변경
    @Transactional
    @Override
    public void updateRefundStatus(Long refundId, RefundRequestDto request) {

        // 환불(refundId) 조회
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_REFUND));

        // 요청됨 상태가 아니면 오류 발생
        if (request.getStatus()!=RefundStatus.PENDING) {
            throw new CustomException(CustomErrorCode.NOT_FOUND_REFUND);
        }

        // 환불 반려 시 반려 사유 없으면 오류 발생
        if (request.getStatus() == RefundStatus.REJECTED && (request.getReason() == null || request.getReason().isBlank())) {
            throw new CustomException(CustomErrorCode.NOT_FOUND_REASON);
        }

        // 환불 상태 변경 및 저장
        refund.updateStatus(refund.getStatus(), refund.getReason());
        refundRepository.save(refund);
    }


}
