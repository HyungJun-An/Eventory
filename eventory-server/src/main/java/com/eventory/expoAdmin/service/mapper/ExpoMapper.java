package com.eventory.expoAdmin.service.mapper;

import com.eventory.common.entity.*;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.expoAdmin.dto.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@AllArgsConstructor
public class ExpoMapper {

    private final ReservationRepository reservationRepository;

    public ExpoResponseDto toExpoResponseDto(Expo expo) {
        return ExpoResponseDto.builder()
                .expoId(expo.getExpoId())
                .expoAdminId(expo.getExpoAdmin().getExpoAdminId())
                .title(expo.getTitle())
                .imageUrl(expo.getImageUrl())
                .description(expo.getDescription())
                .startDate(expo.getStartDate())
                .endDate(expo.getEndDate())
                .location(expo.getLocation())
                .visibility(expo.getVisibility())
                .createdAt(expo.getCreatedAt())
                .updatedAt(expo.getUpdatedAt())
                .displayStartDate(expo.getDisplayStartDate())
                .displayUpdateDate(expo.getDisplayUpdateDate())
                .status(expo.getStatus())
                .reason(expo.getReason())
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

    public PaymentResponseDto toPaymentResponseDto(Reservation reservation) {
        Payment payment = reservation.getPayment();
        User user = reservation.getUser();

        return PaymentResponseDto.builder()
                .name(user.getName())
                .code(reservation.getCode())
                .people(reservation.getPeople())
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .paidAt(payment.getPaidAt())
                .build();
    }

    // 대시보드 카드
    public DashboardResponseDto toDashboardResponseDto(long viewCount, long totalReservation, double checkInRate) {
        return DashboardResponseDto.builder()
                .viewCount(viewCount)
                .totalReservation(totalReservation)
                .checkInRate(checkInRate)
                .build();
    }

    // 막대그래프(일/주/월)
    public ReservationStatResponseDto toReservationStatResponseDto(String label, long reservationCount) {
        return ReservationStatResponseDto.builder()
                .label(label)
                .reservationCount(reservationCount)
                .build();
    }

    // 통계 리포트 행
    public StatReportRowResponseDto toStatReportRowResponseDto(
            String label,
            Long reservationCount,
            Long peopleCount,
            BigDecimal paymentTotal,
            Double avgPeoplePerResv,
            Long cancelledCount
    ) {
        return StatReportRowResponseDto.builder()
                .label(label)
                .reservationCount(reservationCount)
                .peopleCount(peopleCount)
                .paymentTotal(paymentTotal)
                .avgPeoplePerResv(avgPeoplePerResv)
                .cancelledCount(cancelledCount)
                .build();
    }

    // 티켓 비율(파이차트)
    public TicketTypeRatioResponseDto toTicketTypeRatioResponseDto(
            String type,
            Long reservationCount,
            Long peopleCount,
            Double percentage
    ) {
        return TicketTypeRatioResponseDto.builder()
                .type(type)
                .reservationCount(reservationCount)
                .peopleCount(peopleCount)
                .percentage(percentage)
                .build();
    }

    public ManagerResponseDto toManagerResponseDto(ExpoAdmin expoAdmin) {
        return ManagerResponseDto.builder()
                .expoAdminId(expoAdmin.getExpoAdminId())
                .password(expoAdmin.getPassword())
                .name(expoAdmin.getName())
                .email(expoAdmin.getEmail())
                .phone(expoAdmin.getPhone())
                .createdAt(expoAdmin.getCreatedAt())
                .updatedAt(expoAdmin.getUpdatedAt())
                .build();
    }

    public Banner toBannerRequestDto(Expo expo, BannerCreateRequestDto requestDto) {
        return Banner.builder()
                .expo(expo)
                .payment(null)
                .imageUrl(requestDto.getImageUrl())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .status(BannerStatus.PENDING)
                .build();
    }

    public BannerResponseDto toBannerResponseDto(Banner banner) {
        return BannerResponseDto.builder()
                .bannerId(banner.getBannerId())
                .expoId(banner.getExpo().getExpoId())
                .paymentId(banner.getPayment().getPaymentId())
                .imageUrl(banner.getImageUrl())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .status(banner.getStatus())
                .reason(banner.getReason())
                .build();
    }
}
