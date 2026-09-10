package com.eventory.expoAdmin.service;

import com.eventory.auth.security.CustomUserPrincipal;
import com.eventory.common.entity.*;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.common.repository.ExpoStatisticsRepository;
import com.eventory.common.repository.RefundRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.expoAdmin.dto.PaymentResponseDto;
import com.eventory.expoAdmin.dto.RefundRequestDto;
import com.eventory.expoAdmin.dto.RefundResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.expoAdmin.service.mapper.ExpoMapper;
import com.eventory.payment.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesAdminServiceImpl implements SalesAdminService {

    private final ExpoRepository expoRepository;
    private final ExpoStatisticsRepository expoStatisticsRepository;
    private final RefundRepository refundRepository;
    private final ReservationRepository reservationRepository;
    private final ExpoMapper expoMapper;
    private final PaymentService paymentService;

    // 누적 매출, 총 결제 건수, 총 환불 건수
    @Override
    public SalesResponseDto findSalesStatistics(Long expoAdminId, Long expoId) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

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
    public List<Map<String, Object>> findYearlySales(Long expoAdminId, Long expoId) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

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
    public List<Map<String, Object>> findMonthlySales(Long expoAdminId, Long expoId) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

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
    public List<Map<String, Object>> findDailySales(Long expoAdminId, Long expoId) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

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

    // 결제 내역 관리 - 페이징 O
    @Override
    public Page<PaymentResponseDto> findAllPayments(CustomUserPrincipal expoAdmin, Long expoId, String code, LocalDate startDate, LocalDate endDate, Integer page, Integer size) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdmin.getId());

        int actualPage = (page != null) ? page : 0;
        int actualSize = (size != null) ? size : 10;
        Pageable pageable = PageRequest.of(actualPage, actualSize);

        // 검색어는 예약번호 부분 일치, 날짜는 [시작일 00:00, 종료일+1 00:00) 반열린 구간 → 종료일 당일 결제도 포함
        // (기존에는 LocalDate 를 결제 시각(LocalDateTime)과 직접 비교해 종료일 당일 결제가 빠졌다)
        String keyword = (code == null || code.isBlank()) ? null : code.trim();
        LocalDateTime startAt = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endAt = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        Page<Reservation> reservations = reservationRepository.findByExpoIdAndReservationCode(expoId, keyword, startAt, endAt, pageable);

        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return reservations.map(expoMapper::toPaymentResponseDto);
    }

    // 결제 내역 관리 - 페이징 X
    @Override
    public List<PaymentResponseDto> findAllPayments(CustomUserPrincipal expoAdmin, Long expoId) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdmin.getId());

        // 특정 박람회(expoId)에 해당하는 예약 조회
        List<Reservation> reservations = reservationRepository.findByExpoIdAndReservation(expoId);

        // 스트림 각 요소를 dto객체로 변환 후 다시 List로 반환
        return reservations.stream()
                .map(expoMapper::toPaymentResponseDto)
                .collect(Collectors.toList());
    }

    // 결제 내역 다운로드
    @Override
    public Resource downloadPaymentsExcel(List<PaymentResponseDto> paymentResponseDto) {

        // 엑셀 파일 생성 객체
        Workbook workbook = new XSSFWorkbook();

        // 시트 이름 설정
        Sheet sheet = workbook.createSheet("결제 정보");

        // 컬럼 이름 설정
        Row headerRow = sheet.createRow(0);
        String[] headers = {"예약 번호", "예약자명", "예약 인원", "결제 수단", "결제 금액", "결제 시각"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (int i=0; i<paymentResponseDto.size(); i++) {
            PaymentResponseDto responseDto = paymentResponseDto.get(i);
            Row row = sheet.createRow(i+1);

            row.createCell(0).setCellValue(responseDto.getCode());
            row.createCell(1).setCellValue(responseDto.getName());
            row.createCell(2).setCellValue(responseDto.getPeople());
            row.createCell(3).setCellValue(responseDto.getMethod());
            row.createCell(4).setCellValue(responseDto.getAmount().doubleValue());
            row.createCell(5).setCellValue(responseDto.getPaidAt().format(formatter));
        }

        for (int i=0; i<headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            throw new CustomException(CustomErrorCode.EXCEL_CREATION_FAILED);
        }

        return new ByteArrayResource(outputStream.toByteArray());
    }

    // 환불 요청 관리, 환불 대기, 환불 완료
    @Override
    public Page<RefundResponseDto> findAllRefunds(Long expoAdminId, Long expoId, String status, Integer page, Integer size) {
        // 기본키(expoId)로 Expo 조회
        Expo expo = expoRepository.findById(expoId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인
        checkExpo_ExpoAdminAccess(expo.getExpoAdmin().getExpoAdminId(), expoAdminId);

        // 최근 요청이 위로 오도록 정렬, 페이지 정보(totalPages 등)를 함께 내려 프론트 페이지 이동에 사용
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 특정 박람회(expoId)에 해당하는 결제 조회
        List<Long> paymentIds = reservationRepository.findPaymentIdsByExpoId(expoId);
        if (paymentIds.isEmpty()) {
            return Page.empty(pageable);
        }

        RefundStatus targetStatus = parseRefundStatus(status); // null = 전체
        Page<Refund> refunds = (targetStatus == null)
                ? refundRepository.findByPayment_PaymentIdIn(paymentIds, pageable)
                : refundRepository.findByPayment_PaymentIdInAndStatus(paymentIds, targetStatus, pageable);

        return refunds.map(expoMapper::toRefundResponseDto);
    }

    private RefundStatus parseRefundStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }
        try {
            return RefundStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(CustomErrorCode.INVALID_INPUT);
        }
    }

    // 환불 상태 변경 — 승인 시 실제 환불(결제 취소 + 예약 취소 + 정원 복구)까지 수행
    @Transactional
    @Override
    public void updateRefundStatus(Long expoAdminId, Long refundId, RefundRequestDto requestDto) {

        // 환불(refundId) 조회
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_REFUND));
        Reservation reservation = reservationRepository.findByPayment_PaymentId(refund.getPayment().getPaymentId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION));

        // 현재 로그인한 사용자가 박람회 담당자인지 확인 (기존에는 누구의 환불이든 변경 가능했다)
        checkExpo_ExpoAdminAccess(reservation.getExpo().getExpoAdmin().getExpoAdminId(), expoAdminId);

        if (!refund.isPending()) {
            throw new CustomException(CustomErrorCode.REFUND_ALREADY_HANDLED);
        }

        switch (requestDto.getStatus()) {
            case APPROVED -> {
                // 사용자가 적은 환불 사유를 결제사 취소 사유로 사용 (기존에는 승인 시 사유를 null 로 덮어써 DB 오류가 났다)
                paymentService.refund(reservation.getReservationId(), refund.getReason());
                refund.approve();
            }
            case REJECTED -> {
                if (requestDto.getReason() == null || requestDto.getReason().isBlank()) {
                    throw new CustomException(CustomErrorCode.NOT_FOUND_REASON);
                }
                refund.reject(requestDto.getReason());
            }
            default -> throw new CustomException(CustomErrorCode.INVALID_INPUT); // PENDING 으로 되돌리기는 허용하지 않음
        }
    }

    // 현재 로그인한 사용자가 박람회 담당자인지 확인
    private void checkExpo_ExpoAdminAccess(Long expoOwnerAdminId, Long loggedInAdminId) {
        if (!expoOwnerAdminId.equals(loggedInAdminId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_ACCESS);
        }
    }
}
