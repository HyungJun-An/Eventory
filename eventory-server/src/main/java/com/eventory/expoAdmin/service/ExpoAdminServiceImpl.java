package com.eventory.expoAdmin.service;

import com.eventory.expoAdmin.dto.*;
import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoStatistics;
import com.eventory.expoAdmin.repository.*;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    // 일별 예약 수 (이번 주 월~일 요일별 예약 수 (오늘이 포함된 월~일까지 7일간))
    @Override
    public List<ReservationStatResponseDto> getDailyReservationStats(Long expoId) {
        LocalDate today = LocalDate.now(); // 오늘 날짜
        LocalDate monday = today.with(DayOfWeek.MONDAY); // 이번 주의 월요일
        LocalDate sunday = monday.plusDays(6); // 일요일 (월요일 + 6일)

        return IntStream.range(0, 7) // 0부터 6까지 반복 (총 7일)
                .mapToObj(i -> {
                    LocalDate date = monday.plusDays(i); // 월요일부터 하루씩 증가
                    Long count = reservationRepository.countByExpoIdAndCreatedDate(expoId, date);

                    // 각 날짜에 예약된 인원 수 (people 합계 or count)
                    return new ReservationStatResponseDto(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN), count); // 예: 월, 화
                })
                .collect(Collectors.toList());
    }

    // 주별 예약 수 (최근 4주간 주차별 예약 수 (오늘 기준 최근 4주 (주 단위 구간)))
    @Override
    public List<ReservationStatResponseDto> getWeeklyReservationStats(Long expoId) {
        LocalDate today = LocalDate.now();
        LocalDate endOfLastWeek = today.with(DayOfWeek.SUNDAY); // 오늘이 포함된 주의 일요일
        List<ReservationStatResponseDto> result = new ArrayList<>();

        for (int i = 3; i >= 0; i--) {
            LocalDate start = endOfLastWeek.minusWeeks(i).with(DayOfWeek.MONDAY); // 주 시작일
            LocalDate end = start.plusDays(6); // 주 종료일 (일요일)
            Long count = reservationRepository.countByExpoIdAndDateRange(expoId, start, end);
            String label = start.format(DateTimeFormatter.ofPattern("M/d")) + "~" + end.format(DateTimeFormatter.ofPattern("d")); // 예: 7/15~21
            result.add(new ReservationStatResponseDto(label, count));
        }
        return result;
    }

    // 월별 예약 수 (최근 4개월 간 월별 예약 수 (오늘 기준 최근 4개월 (월 단위))
    @Override
    public List<ReservationStatResponseDto> getMonthlyReservationStats(Long expoId) {
        YearMonth currentMonth = YearMonth.now(); // 기준: 현재 월
        List<ReservationStatResponseDto> result = new ArrayList<>();

        for (int i = 3; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i); // 3개월 전부터 이번 달까지 반복
            LocalDate start = month.atDay(1); // 해당 월의 1일
            LocalDate end = month.atEndOfMonth(); // 해당 월의 말일
            Long count = reservationRepository.countByExpoIdAndDateRange(expoId, start, end);
            String label = month.getMonth().getDisplayName(TextStyle.SHORT, Locale.KOREAN); // 예: 5월, 6월
            result.add(new ReservationStatResponseDto(label, count));
        }
        return result;
    }

    // 공통 통계 생성기 (지정된 기간의 통계를 조회해 DTO로 변환)
    @Override
    public StatReportRowResponseDto buildStatDto(Long expoId, LocalDate start, LocalDate end, String label) {
        Long reservedCount = reservationRepository.countReservations(expoId, start, end); // 해당 기간 내 예약 건 수
        Long people = reservationRepository.sumPeople(expoId, start, end); // 총 예약 인원 수
        BigDecimal total = reservationRepository.sumPayments(expoId, start, end); // 총 결제 금액
        Long cancelled = reservationRepository.countCancelled(expoId, start, end); // 예약 취소 건 수
        double avg = (reservedCount == 0) ? 0 : (double) people / reservedCount; // 예약 건당 평균 인원 수 계산 (단체/개별 관람 비율 파악)

        return new StatReportRowResponseDto(label, reservedCount, people, total, avg, cancelled);
    }

    // 일간 통계 리포트 (이번 주 월~일요일까지 하루 단위로 7개의 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> getDailyReportData(Long expoId) {
        LocalDate today = LocalDate.now(); // 오늘 날짜
        LocalDate monday = today.with(DayOfWeek.MONDAY); // 이번 주의 월요일
        return IntStream.range(0, 7)
                .mapToObj(i -> {
                    LocalDate date = monday.plusDays(i); // 월요일부터 일요일까지 하루씩 증가
                    // 같은 날짜로 start, end 지정해서 하루치 통계 계산 // label 예 : "월요일", "화요일"
                    return buildStatDto(expoId, date, date, date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.KOREAN));
                })
                .toList();
    }

    // 주간 통계 리포트 (최근 4주 간, 주 단위(월~일) 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> getWeeklyReportData(Long expoId) {
        LocalDate today = LocalDate.now(); // 오늘 날짜
        LocalDate base = today.with(DayOfWeek.SUNDAY); // 오늘이 포함된 주의 일요일을 기준
        return IntStream.rangeClosed(0, 3) // 가장 최근 주부터 역으로 4주치 계산, i = 0 → 이번주, i = 1 → 저번주,,, 각 주는 월~일로 구성
                .mapToObj(i -> {
                    LocalDate start = base.minusWeeks(i).with(DayOfWeek.MONDAY);
                    LocalDate end = start.plusDays(6);
                    // label 예 : "7/22 ~ 28"
                    String label = start.format(DateTimeFormatter.ofPattern("M/d")) + " ~ " + end.format(DateTimeFormatter.ofPattern("d"));
                    return buildStatDto(expoId, start, end, label);
                })
                .toList();
    }

    // 월간 통계 리포트 (최근 4개월 간, 월 단위 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> getMonthlyReportData(Long expoId) {
        YearMonth currentMonth = YearMonth.now(); // 기준: 현재 월
        return IntStream.rangeClosed(0, 3) // i=0 → 3개월 전, i=1 → 2개월 전,,,
                .mapToObj(i -> {
                    YearMonth month = currentMonth.minusMonths(3 - i);
                    LocalDate start = month.atDay(1);
                    LocalDate end = month.atEndOfMonth();
                    // label 예 : "2025년 6월"
                    String label = month.format(DateTimeFormatter.ofPattern("yyyy년 M월"));
                    return buildStatDto(expoId, start, end, label);
                })
                .toList();
    }

    // 내부 헬퍼 메서드
    // period(daily/weekly/monthly)에 따라 실제 통계 데이터 조회
    // private List<StatReportRowResponseDto> getReportData(Long expoId, String period) {}

    // 통계 데이터를 .csv 형식으로 응답 (텍스트 기반)
    // @Override
    // public void exportCsvReport(Long expoId, String period, HttpServletResponse response) {}

    // 통계 데이터를 .xlsx 형식으로 응답 (엑셀 전용 포맷)
    // @Override
    // public void exportExcelReport(Long expoId, String period, HttpServletResponse response) {}

}
