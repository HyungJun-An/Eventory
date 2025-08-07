package com.eventory.expoAdmin.service.mapper;

import com.eventory.common.entity.Payment;
import com.eventory.common.entity.Refund;
import com.eventory.common.entity.Reservation;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.common.entity.Expo;
import com.eventory.expoAdmin.dto.RefundResponseDto;
import com.eventory.expoAdmin.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ExpoMapper {

    private final ReservationRepository reservationRepository;

    public ExpoResponseDto toExpoResponseDto(Expo expo) {
        return ExpoResponseDto.builder()
                .expoId(expo.getExpoId())
                .expoAdminId(expo.getExpoAdmin().getExpoAdminId())
                .title(expo.getTitle())
                .description(expo.getDescription())
                .startDate(expo.getStartDate())
                .endDate(expo.getEndDate())
                .location(expo.getLocation())
                .visibility(expo.getVisibility())
                .build();
    }

    public RefundResponseDto toRefundResponseDto(Refund refund) {

        Payment payment = refund.getPayment();

        Reservation reservation = reservationRepository
                .findByPayment_PaymentId(payment.getPaymentId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_RESERVATION));

        return RefundResponseDto.builder()
                        .code(reservation.getCode())
                        .method(payment.getMethod())
                        .amount(payment.getAmount())
                        .paidAt(payment.getPaidAt())
                        .reason(refund.getReason())
                        .status(refund.getStatus())
                        .build();
    }
}
