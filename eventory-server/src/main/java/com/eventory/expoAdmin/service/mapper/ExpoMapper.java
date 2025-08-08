package com.eventory.expoAdmin.service.mapper;

import com.eventory.common.entity.*;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.expoAdmin.dto.ExpoResponseDto;
import com.eventory.expoAdmin.dto.RefundResponseDto;
import com.eventory.expoAdmin.dto.SalesResponseDto;
import com.eventory.common.repository.ReservationRepository;
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

    public SalesResponseDto toSalesResponseDto(Long expoId, ExpoStatistics statistics, long refundCount) {
        return SalesResponseDto.builder()
                .expoId(expoId)
                .viewCount(statistics.getViewCount())
                .reservationCount(statistics.getReservationCount())
                .paymentTotal(statistics.getPaymentTotal())
                .refundCount(refundCount)
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
