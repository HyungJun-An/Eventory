package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.expoAdmin.dto.YearlySalesResponseDto;
import com.eventory.expoAdmin.entity.Expo;
import com.eventory.expoAdmin.entity.ExpoStatistics;
import com.eventory.expoAdmin.repository.*;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        List<Expo> expos = expoRepository.findByExpoAdmin_ExpoAdminIdOrderByTitleAsc(expoAdminId);
        return expos.stream()
                .map(expoMapper::toDto)
                .collect(Collectors.toList());
    }

    // 누적 매출, 총 결제 건수, 총 환불 건수
    @Override
    public SalesResponseDto findSalesStatistics(Long expoId) {
        Optional<ExpoStatistics> expoStatistics = expoStatisticsRepository.findById(expoId);
        ExpoStatistics statistics = expoStatistics.get();
        long refundCount = refundRepository.countRefundsByExpoId(expoId);
        return SalesResponseDto.builder()
                .expoId(expoId)
                .viewCount(statistics.getViewCount())
                .reservationCount(statistics.getReservationCount())
                .paymentTotal(statistics.getPaymentTotal())
                .refundCount(refundCount)
                .build();
    }

    // 연간 매출
    @Override
    public List<Map<String, Object>> findYearlySales(Long expoId) {
        List<Object[]> yearlySales = reservationRepository.findYearlySalesByExpoId(expoId);
        return yearlySales.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("year", row[0]);
            map.put("totalAmount", row[1]);
            return map;
        }).collect(Collectors.toList());
    }
}
