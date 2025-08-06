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

import java.util.List;
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

    @Override
    public List<ExpoResponseDto> findAllExpos(Long expoAdminId) {
        List<Expo> expos = expoRepository.findByExpoAdminIdOrderByTitleAsc(expoAdminId);
        return expos.stream()
                .map(expoMapper::toDto)
                .collect(Collectors.toList());
    }

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

    @Override
    public List<YearlySalesResponseDto> findYearlySales(Long expoId) {
        List<YearlySalesResponseDto> yearlySalesResponseDto = reservationRepository.findYearlySalesByExpoId(expoId);

        return yearlySalesResponseDto;
    }
}
