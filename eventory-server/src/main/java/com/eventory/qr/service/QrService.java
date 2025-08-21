package com.eventory.qr.service;

import com.eventory.common.entity.*;
import com.eventory.common.repository.*;
import com.eventory.qr.util.QrImageUtil;
import com.eventory.qr.util.QrTokenUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.*;

@Service
@RequiredArgsConstructor
public class QrService {
    private final QrProperties props;
    private final ReservationRepository reservationRepository;
    private final QrCodeRepository qrCodeRepository;
    private final TicketRepository ticketRepository;
    private final JavaMailSender mailSender;

    /** 결제 완료 직후 호출: QR 생성, 저장, 메일 발송 */
    @Transactional
    public void issueAndSend(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음: " + reservationId));
        // 이미 발급되어 있으면 재사용(재발송만 수행)
        QrCode qr = qrCodeRepository.findByReservation(res).orElse(null);

        if (qr == null) {
            long exp = calcExpoExpireEpoch(res.getExpo());
            String token = QrTokenUtil.buildToken(res.getReservationId(), res.getCode(), exp, props.getSecret());
            if (token.length() > 255) throw new IllegalStateException("QR 데이터 길이 초과");

            qr = new QrCode();
            qr.setReservation(res);
            qr.setData(token);
            qr.setStatus(QrCodeStatus.PENDING);
            qr = qrCodeRepository.save(qr);

            ticketRepository.save(Ticket.builder().qrCode(qr).status(false).build());
        }
        // 메일 발송
        byte[] png = QrImageUtil.toPng(qr.getData(), 320);
        sendMailWithInlineImage(res, png);
    }

    private long calcExpoExpireEpoch(Expo expo) {
        // 유효기간: 박람회 마지막날 23:59:59 (Asia/Seoul)
        LocalDate end = expo.getEndDate();
        LocalDateTime endOfDay = end.atTime(23, 59, 59);
        ZonedDateTime z = endOfDay.atZone(ZoneId.of("Asia/Seoul"));
        return z.toEpochSecond();
    }

    private void sendMailWithInlineImage(Reservation res, byte[] png) {
        try {
            var msg = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(msg, true, StandardCharsets.UTF_8.name());
            helper.setFrom(props.getMailFrom(), props.getIssuer());
            helper.setTo(res.getUser().getEmail());
            helper.setSubject("[" + res.getExpo().getTitle() + "] 모바일 입장권 (QR)");

            String html = """
                    <div style='font-family:Arial,Helvetica,sans-serif'>
                      <h2>입장 QR 티켓</h2>
                      <p>행사명: <b>%s</b></p>
                      <p>예약번호: <b>%d</b> / 예약코드: <b>%s</b></p>
                      <p>유효기간: 박람회 종료일까지 (재입장 불가)</p>
                      <img src='cid:qrImage' alt='QR' style='width:260px;height:260px;border:1px solid #eee;border-radius:12px'/>
                      <p style='color:#666;font-size:12px'>현장에서 이 QR을 제시해 주세요.</p>
                    </div>
                    """.formatted(res.getExpo().getTitle(), res.getReservationId(), res.getCode());
            helper.setText(html, true);
            helper.addInline("qrImage", new org.springframework.core.io.ByteArrayResource(png), "image/png");
            mailSender.send(msg);
        } catch (Exception e) {
            throw new IllegalStateException("QR 메일 발송 실패", e);
        }
    }
}