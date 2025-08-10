package com.eventory.expoAdmin.service;

import com.eventory.common.entity.Refund;
import com.eventory.common.entity.RefundStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.*;
import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoStatistics;
import com.eventory.expoAdmin.repository.*;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
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


    // expoId 유효/존재 검증
    private void assertValidExpoId(Long expoId) {
        if (expoId == null || expoId <= 0) {
            throw new CustomException(CustomErrorCode.INVALID_ID);
        }
        if (!expoRepository.existsById(expoId)) {
            throw new CustomException(CustomErrorCode.EXPO_NOT_FOUND);
        }
    }

    // start/end null 또는 start > end 방지
    private void assertValidRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new CustomException(CustomErrorCode.INVALID_DATE_RANGE);
        }
    }

    // 대시보드 카드
    @Override
    public DashboardResponseDto getDashboardSummary(Long expoId) {
        assertValidExpoId(expoId);
        // 페이지 조회 수
        Long viewCount = expoStatisticsRepository.findById(expoId)
                .map(ExpoStatistics::getViewCount)
                .orElse(0L);

        // 총 예약 인원 수 (status = RESERVED)
        Long totalReservedPeople = reservationRepository.countReservedPeopleByExpoId(expoId);

        // 입장한 티켓 수
        Long checkedIn = checkInLogRepository.countCheckedInByExpoId(expoId);

        // 입장률 계산
        double checkInRate = (totalReservedPeople == 0) ? 0.0 :
                ((double) checkedIn / totalReservedPeople) * 100;

        return expoMapper.toDashboardResponseDto(viewCount, totalReservedPeople, checkInRate);
    }

    // 날짜 포맷터 상수
    private static final Locale KO = Locale.KOREAN;
    private static final DateTimeFormatter DAILY_FMT = DateTimeFormatter.ofPattern("MM/dd (E)", KO);
    private static final DateTimeFormatter WEEK_MD   = DateTimeFormatter.ofPattern("M/d", KO);
    private static final DateTimeFormatter MONTH_YM  = DateTimeFormatter.ofPattern("M월", KO);

    // 하루 라벨: 08/09 (토)
    private String labelDaily(LocalDate date) {
        return DAILY_FMT.format(date);
    }

    // 주 라벨: 같은 달: "7/15 ~ 21" / 다른 달: "7/29 ~ 8/4"
    private String labelWeek(LocalDate start, LocalDate end) {
        String left = WEEK_MD.format(start);
        String right = (start.getMonth() == end.getMonth())
                ? String.valueOf(end.getDayOfMonth())
                : WEEK_MD.format(end);
        return left + " ~ " + right;
    }

    // 월 라벨: 8월
    private String labelMonth(YearMonth ym) {
        return MONTH_YM.format(ym.atDay(1));
    }

    // 일별 예약 수 (이번 주 월~일 요일별 예약 수 (오늘이 포함된 월~일까지 7일간))
    @Override
    public List<ReservationStatResponseDto> getDailyReservationStats(Long expoId) {
        assertValidExpoId(expoId);
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY); // 이번 주의 월요일

        return IntStream.range(0, 7) // 0부터 6까지 반복 (총 7일)
                .mapToObj(i -> {
                    LocalDate date = monday.plusDays(i); // 월요일부터 하루씩 증가
                    Long count = reservationRepository.countByExpoIdAndCreatedDate(expoId, date);

                    // 각 날짜에 예약된 인원 수
                    return expoMapper.toReservationStatResponseDto(labelDaily(date), count);
                })
                .toList();
    }

    // 주별 예약 수 (최근 4주간 주차별 예약 수 (오늘 기준 최근 4주 (주 단위 구간)))
    @Override
    public List<ReservationStatResponseDto> getWeeklyReservationStats(Long expoId) {
        assertValidExpoId(expoId);
        LocalDate baseSunday = LocalDate.now().with(DayOfWeek.SUNDAY); // 오늘이 포함된 주의 일요일

        List<ReservationStatResponseDto> result = new ArrayList<>();

        // 오래된주 → 최신주 (오른쪽이 최신)
        for (int i = 3; i >= 0; i--) {
            LocalDate start = baseSunday.minusWeeks(i).with(DayOfWeek.MONDAY); // 주 시작일
            LocalDate end = start.plusDays(6); // 주 종료일 (일요일)
            assertValidRange(start, end);
            Long count = reservationRepository.countByExpoIdAndDateRange(expoId, start, end);

            result.add(expoMapper.toReservationStatResponseDto(labelWeek(start, end), count));
        }
        return result;
    }

    // 월별 예약 수 (최근 4개월 간 월별 예약 수 (오늘 기준 최근 4개월 (월 단위))
    @Override
    public List<ReservationStatResponseDto> getMonthlyReservationStats(Long expoId) {
        assertValidExpoId(expoId);
        YearMonth currentMonth = YearMonth.now(); // 기준: 현재 월
        List<ReservationStatResponseDto> result = new ArrayList<>();

        for (int i = 3; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i); // 3개월 전부터 이번 달까지 반복
            LocalDate start = month.atDay(1); // 해당 월의 1일
            LocalDate end = month.atEndOfMonth(); // 해당 월의 말일
            assertValidRange(start, end);
            Long count = reservationRepository.countByExpoIdAndDateRange(expoId, start, end);

            result.add(expoMapper.toReservationStatResponseDto(labelMonth(month), count));
        }
        return result;
    }

    // 공통 통계 생성기 (지정된 기간의 통계를 조회해 DTO로 변환)
    @Override
    public StatReportRowResponseDto buildStatDto(Long expoId, LocalDate start, LocalDate end, String label) {
        assertValidExpoId(expoId);
        assertValidRange(start, end);
        Long reservedCount = reservationRepository.countReservations(expoId, start, end); // 해당 기간 내 예약 건 수
        Long people = reservationRepository.sumPeople(expoId, start, end); // 총 예약 인원 수
        BigDecimal total = reservationRepository.sumPayments(expoId, start, end); // 총 결제 금액
        Long cancelled = reservationRepository.countCancelled(expoId, start, end); // 예약 취소 건 수
        double avg = (reservedCount == 0) ? 0 : (double) people / reservedCount; // 예약 건당 평균 인원 수 계산 (단체/개별 관람 비율 파악)

        return expoMapper.toStatReportRowResponseDto(
                label,
                reservedCount,
                people,
                total,
                avg,
                cancelled
        );
    }

    // 일간 통계 리포트 (이번 주 월~일요일까지 하루 단위로 7개의 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> getDailyReportData(Long expoId) {
        assertValidExpoId(expoId);
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY); // 이번 주의 월요일

        return IntStream.range(0, 7)
                .mapToObj(i -> {
                    LocalDate date = monday.plusDays(i); // 월요일부터 일요일까지 하루씩 증가
                    // 같은 날짜로 start, end 지정해서 하루치 통계 계산
                    return buildStatDto(expoId, date, date, labelDaily(date));
                })
                .toList();
    }

    // 주간 통계 리포트 (최근 4주 간, 주 단위(월~일) 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> getWeeklyReportData(Long expoId) {
        assertValidExpoId(expoId);
        LocalDate baseSunday = LocalDate.now().with(DayOfWeek.SUNDAY); // 오늘이 포함된 주의 일요일을 기준

        // 최신주부터 쌓임(첫 행이 최신)
        return IntStream.rangeClosed(0, 3) // 가장 최근 주부터 역으로 4주치 계산, i = 0 → 이번주, i = 1 → 저번주,,, 각 주는 월~일로 구성
                .mapToObj(i -> {
                    LocalDate start = baseSunday.minusWeeks(i).with(DayOfWeek.MONDAY);
                    LocalDate end = start.plusDays(6);

                    return buildStatDto(expoId, start, end, labelWeek(start, end));
                })
                .toList();
    }

    // 월간 통계 리포트 (최근 4개월 간, 월 단위 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> getMonthlyReportData(Long expoId) {
        assertValidExpoId(expoId);
        YearMonth currentMonth = YearMonth.now(); // 기준: 현재 월

        return IntStream.rangeClosed(0, 3) // i=0 → 3개월 전, i=1 → 2개월 전,,,
                .mapToObj(i -> {
                    YearMonth month = currentMonth.minusMonths(3 - i);
                    LocalDate start = month.atDay(1);
                    LocalDate end = month.atEndOfMonth();

                    return buildStatDto(expoId, start, end, labelMonth(month));
                })
                .toList();
    }

    // 내부 헬퍼 메서드
    // period(daily/weekly/monthly)에 따라 실제 통계 데이터 조회
    private List<StatReportRowResponseDto> getReportData(Long expoId, String period) {
        if (period == null) throw new CustomException(CustomErrorCode.INVALID_PERIOD);

        return switch (period.toLowerCase()) {
            case "daily" -> getDailyReportData(expoId);
            case "weekly" -> getWeeklyReportData(expoId);
            case "monthly" -> getMonthlyReportData(expoId);
            default -> throw new CustomException(CustomErrorCode.INVALID_PERIOD);
        };
    }

    // 박람회명 조회 (파일 제목 삽입용)
    private String getExpoName(Long expoId) {
        return expoRepository.findById(expoId)
                .map(Expo::getTitle)
                .orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));
    }

    // 기간 컬럼 헤더 라벨:
    private String resolvePeriodHeader(String period) {
        String p = period == null ? "" : period.toLowerCase();
        return switch (p) {
            case "daily" -> "날짜(요일)";
            case "weekly" -> "주차(기간)";
            case "monthly" -> "월";
            default -> throw new CustomException(CustomErrorCode.INVALID_PERIOD);
        };
    }

    // 파일명으로 쓰기에 위험한 문자들을 안전한 문자(_)로 치환
    private String sanitizeForFilename(String name) {
        if (name == null || name.isBlank()) return "expo"; // 공백만 있거나 null이면 기본값("expo")로 대체
        // 윈도우 금지문자(\ / : * ? " < > |) 안전한 문자(_)로 치환
        return name.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
    }

    // DTO 값을 셀에 넣기 전에 NPE 방지로 감싸는 용도
    private long safeLong(Long v) {
        return v == null ? 0L : v; // Long 래퍼(쿼리/집계 결과 등)가 null일 수 있으니 0L로 안전 변환 (숫자 필드에 바로 넣기 좋음)
    }

    // 통계 데이터를 .csv 형식으로 응답 (텍스트 기반)
    @Override
    public void exportCsvReport(Long expoId, String period, HttpServletResponse response) throws IOException {
        String expoName = getExpoName(expoId); // 박람회명 조회
        List<StatReportRowResponseDto> data = getReportData(expoId, period); // 일/주/월 기준에 따라 통계 데이터를 조회

        String safeExpoName = sanitizeForFilename(expoName);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 오늘 날짜 포맷팅
        String baseName = safeExpoName + "-" + period + "-dashboard-report-" + dateStr + ".csv"; // 박람회명-period-dashboard-report-2025-08-08

        String headerLabel = resolvePeriodHeader(period);

        response.setContentType("text/csv; charset=UTF-8");
        // RFC5987: filename*로 UTF-8 안전 적용 + filename 폴백
        String encoded = URLEncoder.encode(baseName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + baseName + "\"; filename*=UTF-8''" + encoded);

        ServletOutputStream os = response.getOutputStream();
        // UTF-8 BOM (윈도우 엑셀 한글 깨짐 방지)
        os.write(0xEF); os.write(0xBB); os.write(0xBF);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8), true)) {
            // 헤더: 기간 라벨은 period에 따라 변경
            writer.println(headerLabel + ",예약 건수,총 예약 인원,총 결제 금액,예약당 평균 인원 수,예약 취소 건수");

            // CSV 접미사 포함(원, 명, 건)
            for (StatReportRowResponseDto row : data) {
                writer.printf("%s,%d,%d,%,.0f원,%.2f명,%d건%n", // 각각의 통계 데이터를 줄 단위로 출력
                        row.getLabel(),
                        row.getReservationCount(),
                        row.getPeopleCount(),
                        row.getPaymentTotal(),
                        row.getAvgPeoplePerResv(),
                        row.getCancelledCount()
                );
            }
        } catch (IOException e) {
            throw new CustomException(CustomErrorCode.REPORT_EXPORT_FAILED);
        }
    }

    // 통계 데이터를 .xlsx 형식으로 응답 (엑셀 전용 포맷)
    @Override
    public void exportExcelReport(Long expoId, String period, HttpServletResponse response) throws IOException {
        String expoName = getExpoName(expoId); // 박람회명 조회
        List<StatReportRowResponseDto> data = getReportData(expoId, period); // 일/주/월 기준에 따라 통계 데이터를 조회

        String safeExpoName = sanitizeForFilename(expoName);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 오늘 날짜 포맷팅
        String baseName = safeExpoName + "-" + period + "-dashboard-report-" + dateStr + ".xlsx"; // 박람회명-period-dashboard-report-2025-08-08

        String headerLabel = resolvePeriodHeader(period);

        try (Workbook workbook = new XSSFWorkbook()) { // 엑셀 새 워크북 생성
            // 시트명은 31자 제한(Excel 포맷 자체 제약)/엑셀 시트명 금지문자 처리
            String sheetName = (period + "-report").replaceAll("[\\\\/*?:\\[\\]]", "_");
            if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);
            Sheet sheet = workbook.createSheet(sheetName); // 시트 생성

            // 데이터 포맷/스타일
            DataFormat df = workbook.createDataFormat();

            // 통화(정수, 천단위) 스타일: #,##0
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(df.getFormat("#,##0"));

            // 평균(소수 둘째 자리) 스타일: 0.00
            CellStyle avgStyle = workbook.createCellStyle();
            avgStyle.setDataFormat(df.getFormat("0.00"));

            // 헤더
            Row header = sheet.createRow(0);
            String[] columns = { headerLabel, "예약 건수", "총 예약 인원", "총 결제 금액", "예약당 평균 인원 수", "예약 취소 건수" };
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]); // 헤더 셀에 컬럼 이름 입력
            }

            // 본문
            int rowNum = 1;
            for (StatReportRowResponseDto row : data) { // DTO 데이터를 Excel 각 셀에 삽입
                Row excelRow = sheet.createRow(rowNum++);
                // 기간(라벨)은 이미 서비스에서 포맷된 문자열로
                excelRow.createCell(0).setCellValue(row.getLabel());

                excelRow.createCell(1).setCellValue(safeLong(row.getReservationCount()));
                excelRow.createCell(2).setCellValue(safeLong(row.getPeopleCount()));

                Cell paymentCell = excelRow.createCell(3);
                paymentCell.setCellValue(row.getPaymentTotal() == null ? 0d : row.getPaymentTotal().doubleValue());
                paymentCell.setCellStyle(currencyStyle);

                Cell avgCell = excelRow.createCell(4);
                avgCell.setCellValue(row.getAvgPeoplePerResv() == null ? 0d : row.getAvgPeoplePerResv());
                avgCell.setCellStyle(avgStyle);

                excelRow.createCell(5).setCellValue(safeLong(row.getCancelledCount()));
            }

            // 가독성: 자동 너비
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String encoded = URLEncoder.encode(baseName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + baseName + "\"; filename*=UTF-8''" + encoded);

            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new CustomException(CustomErrorCode.REPORT_EXPORT_FAILED);
        }

    }

}
