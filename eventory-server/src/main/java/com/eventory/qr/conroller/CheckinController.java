package com.eventory.qr.conroller;

import com.eventory.common.entity.*;
import com.eventory.common.entity.QrProperties;
import com.eventory.common.repository.*;
import com.eventory.qr.dto.CheckinResponse;
import com.eventory.qr.dto.CheckinResult;
import com.eventory.qr.dto.ScanBody;
import com.eventory.qr.util.QrTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 체크인 (QR 스캔 결과 토큰을 전달받아 검증/처리)
 */
@RestController
@RequestMapping("/api/checkin")
@RequiredArgsConstructor
public class CheckinController {
    private final QrProperties props;
    private final QrCodeRepository qrCodeRepository;
    private final TicketRepository ticketRepository;
    private final CheckInLogRepository checkinLogRepository;
    private final ReservationRepository reservationRepository;

    @PostMapping("/scan")
    @Transactional
    public ResponseEntity<CheckinResponse> scan(@RequestBody @Validated ScanBody body) {
        String token = body.getToken();
        if (!QrTokenUtil.verify(token, props.getSecret())) {
            return ResponseEntity.badRequest().body(CheckinResponse.of("INVALID_OR_EXPIRED", null));
        }
        var map = QrTokenUtil.parse(token);
        Long reservationId = Long.valueOf(map.get("r"));

        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음"));

        // QR 행 찾기
        QrCode qr = qrCodeRepository.findByReservation(res)
                .orElseThrow(() -> new IllegalStateException("QR 미발급"));

        if (!token.equals(qr.getData())) {
            return ResponseEntity.badRequest().body(CheckinResponse.of("TOKEN_MISMATCH", null));
        }
        if (qr.getStatus() == QrCodeStatus.CHECKED_IN) {
            return ResponseEntity.badRequest().body(CheckinResponse.of("ALREADY_CHECKED_IN", null));
        }

        // 1회 사용 처리: Ticket.status true, Qr.status CHECKED_IN, 로그 기록
        Ticket ticket = ticketRepository.findByQrCode_QrId(qr.getQrId())
                .orElseThrow(() -> new IllegalStateException("티켓 미존재"));
        ticket.setStatus(true);
        qr.setStatus(QrCodeStatus.CHECKED_IN);
        ticketRepository.save(ticket);
        qrCodeRepository.save(qr);
        checkinLogRepository.save(CheckInLog.builder()
                .ticketId(ticket.getTicketId())
                .time(LocalDateTime.now())
                .build());

        return ResponseEntity.ok(CheckinResponse.of("OK",
                new CheckinResult(res.getExpo().getTitle(), res.getReservationId(), res.getCode())));
    }
}