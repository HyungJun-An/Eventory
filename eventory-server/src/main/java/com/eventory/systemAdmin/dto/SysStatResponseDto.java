package com.eventory.systemAdmin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SysStatResponseDto {

	private Long totalPaymentAmount;
	private Long totalReservationCount;
	private Long totalCheckInCount;
	private Long todayNewUser;
}
