package com.eventory.qr.service;

import com.eventory.common.entity.CheckInLog;
import com.eventory.common.entity.QrCode;
import com.eventory.common.entity.QrCodeStatus;
import com.eventory.common.entity.QrProperties;
import com.eventory.common.entity.Reservation;
import com.eventory.common.entity.ReservationStatus;
import com.eventory.common.entity.Ticket;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.CheckInLogRepository;
import com.eventory.common.repository.QrCodeRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.common.repository.TicketRepository;
import com.eventory.qr.dto.CheckinResponse;
import com.eventory.qr.dto.CheckinResult;
import com.eventory.qr.util.QrTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 입장(체크인) 처리.
 * - QR 스캔(토큰)과 관리자 수동 체크인(예약자 명단)이 같은 입장 규칙을 쓰도록 한 곳에 모은다.
 */
@Service
@RequiredArgsConstructor
public class CheckinService {

    private final QrProperties props;
    private final ReservationRepository reservationRepository;
    private final QrCodeRepository qrCodeRepository;
    private final TicketRepository ticketRepository;
    private final CheckInLogRepository checkInLogRepository;

    /**
     * QR 스캔 체크인 — 기존 API 계약(status 문자열 응답)을 유지한다.
     * OK / INVALID_OR_EXPIRED / RESERVATION_NOT_FOUND / TOKEN_MISMATCH / ALREADY_CHECKED_IN / RESERVATION_CANCELLED
     */
    @Transactional
    public CheckinResponse checkInByToken(String token) {
        if (!QrTokenUtil.verify(token, props.getSecret())) {
            return CheckinResponse.of("INVALID_OR_EXPIRED", null);
        }
        Long reservationId = Long.valueOf(QrTokenUtil.parse(token).get("r"));
        Reservation reservation = reservationRepository.findById(reservationId).orElse(null);
        if (reservation == null) {
            return CheckinResponse.of("RESERVATION_NOT_FOUND", null);
        }
        QrCode qr = qrCodeRepository.findByReservation(reservation).orElse(null);
        if (qr == null || !token.equals(qr.getData())) {
            return CheckinResponse.of("TOKEN_MISMATCH", null);
        }
        try {
            return CheckinResponse.of("OK", markCheckedIn(reservation, qr));
        } catch (CustomException e) {
            // 쓰기 전에 발생하는 검증 예외이므로 트랜잭션에 영향 없음
            return CheckinResponse.of(e.getErrorCode().name(), null);
        }
    }

    /** 관리자 수동 체크인 (예약자 명단의 [체크인] 버튼) */
    @Transactional
    public CheckinResult checkInByReservation(Reservation reservation) {
        QrCode qr = qrCodeRepository.findByReservation(reservation)
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_ISSUED));
        return markCheckedIn(reservation, qr);
    }

    /** 이미 입장한 예약인지 (취소 가능 여부 판단용) */
    @Transactional(readOnly = true)
    public boolean isCheckedIn(Reservation reservation) {
        return qrCodeRepository.findByReservation(reservation)
                .map(qr -> qr.getStatus() == QrCodeStatus.CHECKED_IN)
                .orElse(false);
    }

    private CheckinResult markCheckedIn(Reservation reservation, QrCode qr) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(CustomErrorCode.RESERVATION_CANCELLED);
        }
        if (qr.getStatus() == QrCodeStatus.CHECKED_IN) {
            throw new CustomException(CustomErrorCode.ALREADY_CHECKED_IN);
        }
        Ticket ticket = ticketRepository.findByQrCode_QrId(qr.getQrId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.QR_NOT_ISSUED));

        // 1회 사용 처리: 티켓 사용 + QR 상태 변경 + 입장 로그
        ticket.setStatus(true);
        qr.setStatus(QrCodeStatus.CHECKED_IN);
        // CheckInLog 는 @MapsId 로 ticket_id 를 PK로 공유한다.
        // ticketId 만 넣으면 식별자가 있는 엔티티로 보고 merge 를 시도해 실패하므로, 연관 엔티티(ticket)를 넣어야 한다.
        checkInLogRepository.save(CheckInLog.builder().ticket(ticket).build());

        return new CheckinResult(reservation.getExpo().getTitle(), reservation.getReservationId(), reservation.getCode());
    }
}
