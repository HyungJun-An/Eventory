package com.eventory.expoAdmin.service;

import com.eventory.qr.dto.CheckinResult;

/** 박람회관리자의 예약 단건 처리 (예약자 명단의 체크인 / 취소 버튼) */
public interface ReservationAdminService {

    CheckinResult checkIn(Long expoAdminId, Long expoId, Long reservationId);

    void cancel(Long expoAdminId, Long expoId, Long reservationId, String reason);
}
