package com.eventory.expoAdmin.service;

import com.eventory.common.entity.*;
import com.eventory.expoAdmin.dto.*;
import com.eventory.common.repository.*;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ExpoAdminServiceImpl implements ExpoAdminService {
    private final ExpoRepository expoRepository;
    private final ExpoStatisticsRepository expoStatisticsRepository;
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
    public DashboardResponseDto findDashboardSummary(Long expoId) {
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

    // 일별 예약 수 (최근 7일간 일별 예약 수 (오늘 기준 지난 7일(6일 전 ~ 오늘)))
    @Override
    public List<ReservationStatResponseDto> findDailyReservationStats(Long expoId) {
        assertValidExpoId(expoId);
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // 총 7일

        return IntStream.rangeClosed(0, 6) // 0..6 (오름차순) => startDate -> today
                .mapToObj(i -> {
                    LocalDate d = startDate.plusDays(i);

                    // [start, end) 반열림 구간으로 하루 범위 설정 (시/분/초 경계 오차 방지)
                    LocalDateTime start = d.atStartOfDay();
                    LocalDateTime end = d.plusDays(1).atStartOfDay();

                    Long peopleSum = reservationRepository
                            .sumPeopleByExpoAndCreatedBetween(expoId, start, end);

                    return expoMapper.toReservationStatResponseDto(labelDaily(d), peopleSum);
                })
                .toList(); // 결과: 순서 — 오늘이 맨 마지막(차트 오른쪽/리스트 하단)
    }

    // 주별 예약 수 (최근 4주간 주차별 예약 수 (오늘 기준 최근 4주 (주 단위 구간)))
    @Override
    public List<ReservationStatResponseDto> findWeeklyReservationStats(Long expoId) {
        assertValidExpoId(expoId);
        LocalDate baseSunday = LocalDate.now().with(DayOfWeek.SUNDAY); // 오늘이 포함된 주의 일요일

        List<ReservationStatResponseDto> result = new ArrayList<>();

        // 오래된 → 최신 (오른쪽이 최신)
        for (int i = 3; i >= 0; i--) {
            LocalDate startDate = baseSunday.minusWeeks(i).with(DayOfWeek.MONDAY); // 주 시작일
            LocalDate endDate   = startDate.plusDays(6); // 주 종료일 (일요일)

            assertValidRange(startDate, endDate);
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end   = endDate.plusDays(1).atStartOfDay(); // [월 00:00, 다음주 월 00:00)

            Long peopleSum = reservationRepository
                    .sumPeopleByExpoAndCreatedBetween(expoId, start, end);

            result.add(expoMapper.toReservationStatResponseDto(labelWeek(startDate, endDate), peopleSum));
        }
        return result;
    }

    // 월별 예약 수 (최근 4개월 간 월별 예약 수 (오늘 기준 최근 4개월 (월 단위))
    @Override
    public List<ReservationStatResponseDto> findMonthlyReservationStats(Long expoId) {
        assertValidExpoId(expoId);
        YearMonth currentMonth = YearMonth.now(); // 기준: 현재 월

        List<ReservationStatResponseDto> result = new ArrayList<>();

        // 오래된 → 최신 (오른쪽이 최신)
        for (int i = 3; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            LocalDate startDate = ym.atDay(1); // 해당 월의 1일
            LocalDate endDate   = ym.atEndOfMonth(); // 해당 월의 말일

            assertValidRange(startDate, endDate);
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end   = endDate.plusDays(1).atStartOfDay(); // [1일 00:00, 다음달 1일 00:00)

            Long peopleSum = reservationRepository
                    .sumPeopleByExpoAndCreatedBetween(expoId, start, end);

            result.add(expoMapper.toReservationStatResponseDto(labelMonth(ym), peopleSum));
        }
        return result;
    }

    // 공통 통계 생성기 (지정된 기간의 통계를 조회해 DTO로 변환)
    @Override
    public StatReportRowResponseDto buildStatDto(Long expoId, LocalDate start, LocalDate end, String label) {
        assertValidExpoId(expoId);
        assertValidRange(start, end);

        // [start 00:00, end 다음날 00:00) 반열림
        LocalDateTime s = start.atStartOfDay();
        LocalDateTime e = end.plusDays(1).atStartOfDay();

        Long reservedCount = reservationRepository.countReservations(expoId, s, e); // 해당 기간 내 예약 건 수
        Long people = reservationRepository.sumPeople(expoId, s, e); // 총 예약 인원 수
        BigDecimal total = reservationRepository.sumPayments(expoId, s, e); // 총 결제 금액
        Long cancelled = reservationRepository.countCancelled(expoId, s, e); // 예약 취소 건 수
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

    // 일간 통계 리포트 (최근 7일간, 오늘 기준 지난 7일(6일 전 ~ 오늘까지 하루 단위로 7개의 통계 리포트 생성))
    @Override
    public List<StatReportRowResponseDto> findDailyReportData(Long expoId) {
        assertValidExpoId(expoId);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6); // 총 7일

        return IntStream.rangeClosed(0, 6) // 0..6 (오름차순) => startDate -> today
                .mapToObj(i -> {
                    LocalDate date = startDate.plusDays(i);
                    return buildStatDto(expoId, date, date, labelDaily(date));
                })
                .toList();
    }

    // 주간 통계 리포트 (최근 4주 간, 주 단위(월~일) 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> findWeeklyReportData(Long expoId) {
        assertValidExpoId(expoId);
        LocalDate baseSunday = LocalDate.now().with(DayOfWeek.SUNDAY); // 오늘이 포함된 주의 일요일을 기준

        List<StatReportRowResponseDto> result = new ArrayList<>();

        for (int i = 3; i >= 0; i--) { // 오래된 주부터
            LocalDate start = baseSunday.minusWeeks(i).with(DayOfWeek.MONDAY);
            LocalDate end   = start.plusDays(6);

            result.add(buildStatDto(expoId, start, end, labelWeek(start, end)));
        }
        return result;
    }

    // 월간 통계 리포트 (최근 4개월 간, 월 단위 통계 리포트 생성)
    @Override
    public List<StatReportRowResponseDto> findMonthlyReportData(Long expoId) {
        assertValidExpoId(expoId);
        YearMonth currentMonth = YearMonth.now(); // 기준: 현재 월

        List<StatReportRowResponseDto> result = new ArrayList<>();

        for (int i = 3; i >= 0; i--) { // 오래된 월부터
            YearMonth ym   = currentMonth.minusMonths(i);
            LocalDate start    = ym.atDay(1);
            LocalDate end    = ym.atEndOfMonth();
            result.add(buildStatDto(expoId, start, end, labelMonth(ym)));
        }
        return result;
    }

    // 내부 헬퍼 메서드
    // period(daily/weekly/monthly)에 따라 실제 통계 데이터 조회
    private List<StatReportRowResponseDto> findReportData(Long expoId, String period) {
        if (period == null) throw new CustomException(CustomErrorCode.INVALID_PERIOD);

        return switch (period.toLowerCase()) {
            case "daily" -> findDailyReportData(expoId);
            case "weekly" -> findWeeklyReportData(expoId);
            case "monthly" -> findMonthlyReportData(expoId);
            default -> throw new CustomException(CustomErrorCode.INVALID_PERIOD);
        };
    }

    // 박람회명 조회 (파일 제목 삽입용)
    private String findExpoName(Long expoId) {
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

    // CSV 값 이스케이프: 값에 쉼표/따옴표/개행이 있으면 따옴표로 감싸고 내부 따옴표는 ""로 이스케이프
    private static String escapeCsv(String value) {
        if (value == null) return "";
        boolean needQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (!needQuote) return value;
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }

    // 통계 데이터를 .csv 형식으로 응답 (텍스트 기반)
    @Override
    public FileDownloadDto exportCsvReport(Long expoId, String period) {
        String expoName = findExpoName(expoId); // 박람회명 조회
        List<StatReportRowResponseDto> data = findReportData(expoId, period); // 일/주/월 기준에 따라 통계 데이터를 조회

        String safeExpoName = sanitizeForFilename(expoName);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 오늘 날짜 포맷팅
        String baseName = safeExpoName + "-" + period + "-dashboard-report-" + dateStr + ".csv"; // 박람회명-period-dashboard-report-2025-08-08

        String headerLabel = resolvePeriodHeader(period);

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw, false)) {

            // UTF-8 BOM (엑셀에서 한글/UTF-8 인식)
            bos.write(0xEF); bos.write(0xBB); bos.write(0xBF);

            // 헤더: 기간 라벨은 period에 따라 변경
            writer.println(String.join(",",
                    headerLabel,
                    "예약 건수",
                    "총 예약 인원",
                    "총 결제 금액(원)",
                    "예약당 평균 인원 수(명)",
                    "예약 취소 건수"
            ));

            // 본문: CSV는 쉼표 분리, 줄바꿈으로 레코드 구분
            for (StatReportRowResponseDto row : data) {
                String label = escapeCsv(row.getLabel()); // 혹시 모를 쉼표/따옴표 대응

                long reservationCount = row.getReservationCount() == null ? 0L : row.getReservationCount();
                long peopleCount      = row.getPeopleCount() == null ? 0L : row.getPeopleCount();

                // BigDecimal → 문자열(손실 없이)
                String paymentTotal = (row.getPaymentTotal() == null)
                        ? "0"
                        : row.getPaymentTotal().setScale(0, RoundingMode.DOWN).toPlainString(); // 소수점 필요 없으면 0자리

                // 평균은 소수 2자리
                String avgPeople = (row.getAvgPeoplePerResv() == null)
                        ? "0.00"
                        : String.format(java.util.Locale.US, "%.2f", row.getAvgPeoplePerResv());

                long cancelledCount  = row.getCancelledCount() == null ? 0L : row.getCancelledCount();

                // 값 쪽에는 단위를 붙이지 않음(숫자로 인식되게)
                writer.println(String.join(",",
                        label,
                        String.valueOf(reservationCount),
                        String.valueOf(peopleCount),
                        paymentTotal,
                        avgPeople,
                        String.valueOf(cancelledCount)
                ));
            }
            writer.flush();

            return new FileDownloadDto(
                    bos.toByteArray(),
                    baseName,
                    "text/csv; charset=UTF-8"
            );
        } catch (IOException e) {
            throw new CustomException(CustomErrorCode.REPORT_EXPORT_FAILED);
        }
    }

    // 통계 데이터를 .xlsx 형식으로 응답 (엑셀 전용 포맷)
    @Override
    public FileDownloadDto exportExcelReport(Long expoId, String period) {
        String expoName = findExpoName(expoId); // 박람회명 조회
        String safeExpoName = sanitizeForFilename(expoName);
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")); // 오늘 날짜 포맷팅
        String baseName = safeExpoName + "-" + period + "-dashboard-report-" + dateStr + ".xlsx"; // 박람회명-period-dashboard-report-2025-08-08

        List<StatReportRowResponseDto> data = findReportData(expoId, period); // 일/주/월 기준에 따라 통계 데이터를 조회
        String headerLabel = resolvePeriodHeader(period);

        try (Workbook workbook = new XSSFWorkbook(); // 엑셀 새 워크북 생성
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            // 시트 생성/스타일
            // 시트명은 31자 제한(Excel 포맷 자체 제약)/엑셀 시트명 금지문자 처리
            String sheetName = (period + "-report").replaceAll("[\\\\/*?:\\[\\]]", "_");
            if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);
            Sheet sheet = workbook.createSheet(sheetName);

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

                excelRow.createCell(0).setCellValue(row.getLabel()); // 기간 라벨

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

            workbook.write(bos);
            return new FileDownloadDto(
                    bos.toByteArray(),
                    baseName,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
        } catch (IOException e) {
            // 생성 중 I/O 문제 → 커스텀 예외로 래핑
            throw new CustomException(CustomErrorCode.REPORT_EXPORT_FAILED);
        }
    }

    // 티켓 종류별(FREE/PAID) 예약 비율 조회 (RESERVED 상태만 집계)
    @Override
    public List<TicketTypeRatioResponseDto> findTicketTypeRatios(Long expoId) {
        assertValidExpoId(expoId);

        try {
            // 티켓 종류별 집계 (무료/유료)
            // 결과: Object[] { type(String), reservationCount(Long), peopleCount(Long) }
            List<Object[]> rows = reservationRepository.aggregateTicketTypeCountsReserved(expoId, BigDecimal.ZERO);

            long totalReservations = 0L; // 총 예약 건수 (비율 계산용)
            Map<String, TicketTypeRatioResponseDto> map = new HashMap<>();

            for (Object[] row : rows) {
                String type = (String) row[0];                  // "FREE" or "PAID"
                Long reservationCount = ((Number) row[1]).longValue(); // 예약 건수
                Long peopleCount = ((Number) row[2]).longValue(); // 예약 인원 수

                // 일단 percentage = 0.0 으로 생성 후 아래에서 비율 계산
                map.put(type, expoMapper.toTicketTypeRatioResponseDto(
                        type, reservationCount, peopleCount, 0.0
                ));

                totalReservations += reservationCount;
            }

            // 누락된 타입(FREE/PAID) 보정 → 항상 두 개의 타입 반환
            map.putIfAbsent("FREE", expoMapper.toTicketTypeRatioResponseDto(
                    "FREE", 0L, 0L, 0.0
            ));
            map.putIfAbsent("PAID", expoMapper.toTicketTypeRatioResponseDto(
                    "PAID", 0L, 0L, 0.0
            ));

            // 비율 계산 (총 예약 건수가 0인 경우 비율은 0) (소수점 둘째 자리까지 반올림)
            if (totalReservations > 0) {
                final long total = totalReservations;
                map.replaceAll((key, dto) -> expoMapper.toTicketTypeRatioResponseDto(
                        dto.getType(),
                        dto.getReservationCount(),
                        dto.getPeopleCount(),
                        Math.round((dto.getReservationCount() * 100.0 / total) * 100) / 100.0
                ));
            }

            // 결과 정렬 (FREE, PAID 순서)
            List<TicketTypeRatioResponseDto> result = new ArrayList<>(map.values());
            result.sort(Comparator.comparing(TicketTypeRatioResponseDto::getType));

            return result;

        } catch (CustomException e) {
            // 이미 서비스 내부에서 의도적으로 던진 예외 → 그대로 다시 던져서 메시지/코드 유지
            throw e;
        } catch (Exception e) {
            // 예상치 못한 모든 예외 → 공통 예외 코드(TICKET_TYPE_STATS_FAILED)로 감싸서 던짐
            throw new CustomException(CustomErrorCode.TICKET_TYPE_STATS_FAILED);
        }
    }
}
