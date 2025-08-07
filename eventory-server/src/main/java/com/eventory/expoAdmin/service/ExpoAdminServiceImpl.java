package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.DashboardResponseDto;
import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoStatistics;
import com.eventory.expoAdmin.repository.*;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
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
    private final CheckInLogRepository checkInLogRepository;
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

    // 월간 매출
    @Override
    public List<Map<String, Object>> findMonthlySales(Long expoId) {
        int currentYear = Year.now().getValue();

        List<Object[]> monthlySales = reservationRepository.findMonthySalesByExpoId(expoId, currentYear);

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
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.minusDays(6).atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<Object[]> result = reservationRepository.findDailySalesLast7Days(expoId, start, end);

        Map<String, Long> salesMap = result.stream().collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> ((Number) row[1]).longValue()
        ));

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

    // 대시보드 카드
    @Override
    public DashboardResponseDto getDashboardSummary(Long expoId) {
        // 페이지 조회 수
        Long viewCount = expoStatisticsRepository.findById(expoId)
                .map(stat -> stat.getViewCount())
                .orElse(0L);

        // 총 예약 인원 수 (status = RESERVED)
        Long totalReservedPeople = reservationRepository.countReservedPeopleByExpoId(expoId);

        // 입장한 티켓 수
        Long checkedIn = checkInLogRepository.countCheckedInByExpoId(expoId);

        // 입장률 계산
        double checkInRate = (totalReservedPeople == 0) ? 0.0 :
                ((double) checkedIn / totalReservedPeople) * 100;

        return new DashboardResponseDto(viewCount, totalReservedPeople, checkInRate);
    }
}
