package com.eventory.systemAdmin.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SysChartResponseDto {

	private List<ChartResponseDto> paymentList;
	private List<ChartResponseDto> reservationList;
	private List<ChartResponseDto> checkInList;
}
