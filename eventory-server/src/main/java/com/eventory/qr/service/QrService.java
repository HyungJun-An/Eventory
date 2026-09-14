package com.eventory.qr.service;

import com.eventory.common.entity.*;
import com.eventory.common.repository.*;
import com.eventory.qr.util.QrImageUtil;
import com.eventory.qr.util.QrTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    // Gmail SMTP 는 로그인 계정 주소로만 발송하므로 보내는 주소(qr.mail-from)가 비어 있으면 계정 주소를 쓴다
    @Value("${spring.mail.username:}")
    private String mailUsername;

    /** 결제 완료 트랜잭션 안에서 호출: QR 토큰·티켓 발급 (이미 발급돼 있으면 재사용) */
    @Transactional
    public QrCode issue(Reservation res) {
        return qrCodeRepository.findByReservation(res).orElseGet(() -> {
            long exp = calcExpoExpireEpoch(res.getExpo());
            String token = QrTokenUtil.buildToken(res.getReservationId(), res.getCode(), exp, props.getSecret());
            if (token.length() > 255) throw new IllegalStateException("QR 데이터 길이 초과");

            QrCode qr = new QrCode();
            qr.setReservation(res);
            qr.setData(token);
            qr.setStatus(QrCodeStatus.PENDING);
            QrCode saved = qrCodeRepository.save(qr);

            ticketRepository.save(Ticket.builder().qrCode(saved).status(false).build());
            return saved;
        });
    }

    /** QR 입장권 메일 발송 — 예약 커밋 이후 별도 스레드에서 호출된다 (TicketMailListener) */
    @Transactional(readOnly = true)
    public void sendTicketMail(Long reservationId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음: " + reservationId));
        QrCode qr = qrCodeRepository.findByReservation(res)
                .orElseThrow(() -> new IllegalStateException("QR 미발급: " + reservationId));
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
            String from = StringUtils.hasText(props.getMailFrom()) ? props.getMailFrom() : mailUsername;
            helper.setFrom(from, props.getIssuer());
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
