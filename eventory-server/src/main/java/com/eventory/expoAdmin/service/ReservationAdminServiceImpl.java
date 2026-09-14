package com.eventory.expoAdmin.service;

import com.eventory.common.entity.Expo;
import com.eventory.common.entity.Refund;
import com.eventory.common.entity.Reservation;
import com.eventory.common.entity.ReservationStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.RefundRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.payment.service.PaymentService;
import com.eventory.qr.dto.CheckinResult;
import com.eventory.qr.service.CheckinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationAdminServiceImpl implements ReservationAdminService {

    private final ReservationRepository reservationRepository;
    private final RefundRepository refundRepository;
    private final CheckinService checkinService;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public CheckinResult checkIn(Long expoAdminId, Long expoId, Long reservationId) {
        Reservation reservation = findOwnedReservation(expoAdminId, expoId, reservationId);
        return checkinService.checkInByReservation(reservation);
    }

    /**
     * 관리자 예약 취소 = 전액 환불.
     * 결제 취소·상태 변경·정원 복구는 PaymentService.refund 가 담당하고, 여기서는 환불 이력(Refund)을 남겨
     * 환불 처리 화면에서도 확인할 수 있게 한다.
     */
    @Override
    @Transactional
    public void cancel(Long expoAdminId, Long expoId, Long reservationId, String reason) {
        Reservation reservation = findOwnedReservation(expoAdminId, expoId, reservationId);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new CustomException(CustomErrorCode.RESERVATION_CANCELLED);
        }
        if (checkinService.isCheckedIn(reservation)) {
            throw new CustomException(CustomErrorCode.ALREADY_CHECKED_IN); // 입장한 예약은 취소 불가
        }
        paymentService.refund(reservationId, reason);
        refundRepository.save(Refund.approvedOf(reservation.getPayment(), reason));
    }

    /** 예약이 요청한 박람회에 속하고, 그 박람회를 로그인한 관리자가 담당하는지 확인 */
    private Reservation findOwnedReservation(Long expoAdminId, Long expoId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION));
        Expo expo = reservation.getExpo();
        if (!expo.getExpoId().equals(expoId)) {
            throw new CustomException(CustomErrorCode.RESERVATION_NOT_IN_EXPO);
        }
        if (expo.getExpoAdmin() == null || !expo.getExpoAdmin().getExpoAdminId().equals(expoAdminId)) {
            throw new CustomException(CustomErrorCode.FORBIDDEN_ACCESS);
        }
        return reservation;
    }
}
