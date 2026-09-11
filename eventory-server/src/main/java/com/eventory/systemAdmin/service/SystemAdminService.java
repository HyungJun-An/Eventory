package com.eventory.systemAdmin.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserRepository;
import com.eventory.auth.tokenStore.AccountType;
import com.eventory.auth.tokenStore.TokenStore;
import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.ExpoStatus;
import com.eventory.common.entity.Payment;
import com.eventory.common.entity.PaymentStatus;
import com.eventory.common.entity.SystemAdmin;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.CheckInLogRepository;
import com.eventory.common.repository.ExpoAdminRepository;
import com.eventory.common.repository.ExpoRepository;
import com.eventory.common.repository.PaymentRepository;
import com.eventory.common.repository.ReservationRepository;
import com.eventory.expoAdmin.dto.ManagerRequestDto;
import com.eventory.systemAdmin.dto.AdminCredentialDto;
import com.eventory.systemAdmin.dto.ChartResponseDto;
import com.eventory.systemAdmin.dto.ExpoApprovalResponseDto;
import com.eventory.systemAdmin.dto.ExpoStatusRequestDto;
import com.eventory.systemAdmin.dto.SysChartResponseDto;
import com.eventory.systemAdmin.dto.SysExpoAdminResponseDto;
import com.eventory.systemAdmin.dto.SysExpoDetailResponseDto;
import com.eventory.systemAdmin.dto.SysExpoResponseDto;
import com.eventory.systemAdmin.dto.SysMeResponseDto;
import com.eventory.systemAdmin.dto.SysStatResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemAdminService {

	private static final int LOGIN_ID_LENGTH = 8;
	private static final int TEMP_PASSWORD_LENGTH = 12;
	// 최신 신청이 먼저 보이도록 (기존: 정렬 없음 → 오래된 박람회부터 노출)
	private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "createdAt");

	private final ExpoRepository expoRepository;
	private final ExpoAdminRepository expoAdminRepository;
	private final SystemAdminRepository systemAdminRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final PaymentRepository paymentRepository;
	private final ReservationRepository reservationRepository;
	private final CheckInLogRepository checkInLogRepository;
	private final UserRepository userRepository;
	private final TokenStore tokenStore;

	public SysMeResponseDto findMe(Long systemAdminId) {
		SystemAdmin admin = systemAdminRepository.findById(systemAdminId)
				.orElseThrow(() -> new CustomException(CustomErrorCode.MEMBER_NOT_EXIST));
		return new SysMeResponseDto(admin.getSystemAdminId(), admin.getCustomerId(), admin.getName(), admin.getEmail());
	}

	public Page<SysExpoResponseDto> findAllSysExpoPages(String status, String title, int page, int size) {

		Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);
		Page<Expo> expoPage;

		if(status != null && !status.isBlank()) {
			if(title != null && !title.isBlank()) {
				expoPage = expoRepository.findByStatusAndTitleContaining(ExpoStatus.valueOf(status), title, pageable);
			} else {
				expoPage = expoRepository.findByStatus(ExpoStatus.valueOf(status), pageable);
			}
		} else if(title != null && !title.isBlank()) {
			expoPage = expoRepository.findByTitleContaining(title, pageable);
		}
		else {
			expoPage = expoRepository.findAll(pageable);
		}

		return expoPage.map(this::toListDto);
	}

	/** 승인 심사용 박람회 상세 (신청 담당자 포함) */
	public SysExpoDetailResponseDto findExpoDetail(Long expoId) {
		Expo expo = expoRepository.findById(expoId).orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

		// 승인 전에는 신청 시 만든 임시 관리자(customerId = 박람회 제목)가 담당자
		ExpoAdmin applicant = expo.getExpoAdmin() != null
				? expo.getExpoAdmin()
				: expoAdminRepository.findByCustomerId(expo.getTitle()).orElse(null);

		return SysExpoDetailResponseDto.builder()
				.id(expo.getExpoId())
				.title(expo.getTitle())
				.description(expo.getDescription())
				.imageUrl(expo.getImageUrl())
				.location(expo.getLocation())
				.startDate(expo.getStartDate())
				.endDate(expo.getEndDate())
				.price(expo.getPrice())
				.maxCapacity(expo.getMaxCapacity())
				.reservedCount(expo.getReservedCount())
				.status(expo.getStatus())
				.reason(expo.getReason())
				.categories(expo.getExpoCategories().stream().map(ec -> ec.getCategory().getName()).toList())
				.createdAt(expo.getCreatedAt())
				.applicant(applicant == null ? null : new SysExpoDetailResponseDto.Applicant(
						applicant.getName(), applicant.getEmail(), applicant.getPhone(),
						expo.getExpoAdmin() != null ? applicant.getCustomerId() : null))
				.build();
	}

	/**
	 * 박람회 승인·반려.
	 * 첫 승인이면 임시 관리자에게 로그인 계정을 발급하고, 원문 비밀번호를 응답으로 한 번만 돌려준다.
	 * (기존: 발급만 하고 "이메일 전송" 미구현 → 누구도 비밀번호를 알 수 없어 박람회관리자 로그인 불가)
	 */
	@Transactional
	public ExpoApprovalResponseDto updateExpoStatus(Long expoId, ExpoStatusRequestDto requestDto) {

		Expo expo = expoRepository.findById(expoId).orElseThrow(() -> new CustomException(CustomErrorCode.EXPO_NOT_FOUND));

		if(!expo.getStatus().equals(ExpoStatus.PENDING)) {
			throw new CustomException(CustomErrorCode.HANDLED_EXPO);
		}

		AdminCredentialDto credential = null;

		if(ExpoStatus.APPROVED.name().equals(requestDto.getStatus())) {
			if(expo.getExpoAdmin() == null) {
				ExpoAdmin admin = expoAdminRepository.findByCustomerId(expo.getTitle()).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
				String customerId = RandomGenerator.generateRandomId(LOGIN_ID_LENGTH);
				String rawPassword = RandomGenerator.generateRandomPassword(TEMP_PASSWORD_LENGTH);
				admin.createAccount(customerId, passwordEncoder.encode(rawPassword));
				expo.approveAndConnectAdmin(admin);
				credential = new AdminCredentialDto(customerId, rawPassword);
			} else {
				expo.approve();
			}

		} else if(ExpoStatus.REJECTED.name().equals(requestDto.getStatus())){
			// 기존: isBlank() 를 null 검사보다 먼저 호출해 사유 없이 반려하면 NPE(500)
			if(!StringUtils.hasText(requestDto.getReason())) {
				throw new CustomException(CustomErrorCode.REASON_REQUIRED);
			}

			if(expo.getExpoAdmin() == null) {
				expoAdminRepository.deleteByCustomerId(expo.getTitle());
			}

			expo.reject(requestDto.getReason().trim());
		} else {
			throw new CustomException(CustomErrorCode.INVALID_EXPO_STATUS);
		}

		return new ExpoApprovalResponseDto(expo.getExpoId(), expo.getStatus(), credential);
	}

	public Page<SysExpoAdminResponseDto> findAllExpoAdminPages(String keyword, int page, int size) {

		Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);
		Page<ExpoAdmin> expoAdminPage;

		if(keyword != null && !keyword.isBlank()) {
			expoAdminPage = expoAdminRepository.findByNameContainingOrPhoneContainingOrEmailContaining(keyword, keyword, keyword, pageable);
		} else {
			expoAdminPage = expoAdminRepository.findAll(pageable);
		}

		return expoAdminPage.map(admin -> {
			Expo lastExpo = expoRepository.findFirstByExpoAdminOrderByCreatedAtDesc(admin).orElse(null);
			return SysExpoAdminResponseDto.from(admin, lastExpo != null ? lastExpo.getCreatedAt() : null);
		});
	}

	public Page<SysExpoResponseDto> findExpoByExpoAdminPages(Long adminId, int page, int size) {

		Pageable pageable = PageRequest.of(page, size, NEWEST_FIRST);

		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
		return expoRepository.findByExpoAdmin(admin, pageable).map(this::toListDto);
	}

	public SysExpoAdminResponseDto findExpoAdmin(Long adminId) {

		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));

		return SysExpoAdminResponseDto.from(admin, null);
	}

	@Transactional
	public void updateExpoAdmin(Long adminId, ManagerRequestDto requestDto) {

		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
		admin.updateExpoAdmin(requestDto);
	}

	/**
	 * 임시 비밀번호 재발급 — 기존 세션(리프레시 토큰)도 끊어 이전 비밀번호로 로그인한 사용자를 내보낸다.
	 * (기존 "관리자 계정 재전송" 버튼은 동작 없음)
	 */
	@Transactional
	public AdminCredentialDto resetExpoAdminPassword(Long adminId) {
		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
		if(!expoRepository.existsByExpoAdmin(admin)) {
			throw new CustomException(CustomErrorCode.EXPO_ADMIN_NOT_ACTIVATED); // 승인 전 임시 계정
		}

		String rawPassword = RandomGenerator.generateRandomPassword(TEMP_PASSWORD_LENGTH);
		admin.createAccount(admin.getCustomerId(), passwordEncoder.encode(rawPassword));
		tokenStore.deleteRefreshToken(AccountType.EXPO_ADMIN, adminId);
		return new AdminCredentialDto(admin.getCustomerId(), rawPassword);
	}

	/** 담당 박람회가 있으면 삭제 불가 (기존: expo.expo_admin_id FK 위반으로 500) */
	@Transactional
	public void deleteExpoAdmin(Long adminId) {
		ExpoAdmin admin = expoAdminRepository.findById(adminId).orElseThrow(() -> new CustomException(CustomErrorCode.NOT_FOUND_EXPO_ADMIN));
		if(expoRepository.existsByExpoAdmin(admin)) {
			throw new CustomException(CustomErrorCode.EXPO_ADMIN_HAS_EXPOS);
		}
		expoAdminRepository.delete(admin);
		tokenStore.deleteRefreshToken(AccountType.EXPO_ADMIN, adminId);
	}

	public SysStatResponseDto findSysStat() {

		List<Payment> paymentList = paymentRepository.findAllByStatus(PaymentStatus.PAID);
		Long totalPaymentAmount = paymentList.stream().mapToLong(p -> p.getAmount().longValue()).sum();
		Long totalReservationCount = reservationRepository.count();
		Long totalCheckInCount = checkInLogRepository.count();
		Long todayNewUser = userRepository.countByCreatedAtBetween(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay().minusNanos(1));

		return SysStatResponseDto.builder()
						  .totalPaymentAmount(totalPaymentAmount)
						  .totalReservationCount(totalReservationCount)
						  .totalCheckInCount(totalCheckInCount)
						  .todayNewUser(todayNewUser)
						  .build();
	}

	public SysChartResponseDto findChart(String period) {

		List<ChartResponseDto> paymentList = null;
		List<ChartResponseDto> reservationList = null;
		List<ChartResponseDto> checkInList = null;

		if("monthly".equals(period)) {
			paymentList = paymentRepository.countMonthlyPayments();
			reservationList = reservationRepository.countMonthlyReservations();
			checkInList = checkInLogRepository.countMonthlyCheckIn();
		} else if("weekly".equals(period)) {
			paymentList = paymentRepository.countWeeklyPayments();
			reservationList = reservationRepository.countWeeklyReservations();
			checkInList = checkInLogRepository.countWeeklyCheckIn();
		} else {
			paymentList = paymentRepository.countDailyPayments();
			reservationList = reservationRepository.countDailyReservations();
			checkInList = checkInLogRepository.countDailyCheckIn();
		}

		return SysChartResponseDto.builder()
								  .paymentList(paymentList)
								  .reservationList(reservationList)
								  .checkInList(checkInList)
								  .build();
	}

	/**
	 * 박람회는 카테고리를 여러 개 가질 수 있다 (expo_category N:M).
	 * 기존: expoCategoryRepository.findByExpo() 가 Optional 단건 조회라 카테고리가 2개 이상이면 NonUniqueResult → 목록 전체 500
	 */
	private SysExpoResponseDto toListDto(Expo expo) {
		String categories = expo.getExpoCategories().stream()
				.map(ec -> ec.getCategory().getName())
				.collect(Collectors.joining(", "));
		return SysExpoResponseDto.from(expo, categories.isEmpty() ? "없음" : categories);
	}

}
