package com.eventory.systemAdmin.service;

import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserRepository;
import com.eventory.auth.tokenStore.AccountType;
import com.eventory.auth.tokenStore.TokenStore;
import com.eventory.common.entity.Expo;
import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.ExpoStatus;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.*;
import com.eventory.systemAdmin.dto.ExpoApprovalResponseDto;
import com.eventory.systemAdmin.dto.ExpoStatusRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SystemAdminServiceTest {

    @Mock ExpoRepository expoRepository;
    @Mock ExpoAdminRepository expoAdminRepository;
    @Mock SystemAdminRepository systemAdminRepository;
    @Mock BCryptPasswordEncoder passwordEncoder;
    @Mock PaymentRepository paymentRepository;
    @Mock ReservationRepository reservationRepository;
    @Mock CheckInLogRepository checkInLogRepository;
    @Mock UserRepository userRepository;
    @Mock TokenStore tokenStore;

    @InjectMocks SystemAdminService service;

    @Test
    @DisplayName("첫 승인이면 관리자 계정을 발급하고 원문 비밀번호를 한 번만 돌려준다 (DB 에는 해시만 저장)")
    void approve_issuesCredential() {
        Expo expo = pendingExpo("스마트시티 엑스포");
        ExpoAdmin applicant = ExpoAdmin.builder().expoAdminId(5L).customerId("스마트시티 엑스포").name("신청자").build();
        when(expoRepository.findById(1L)).thenReturn(Optional.of(expo));
        when(expoAdminRepository.findByCustomerId("스마트시티 엑스포")).thenReturn(Optional.of(applicant));
        when(passwordEncoder.encode(anyString())).thenReturn("bcrypt-hash");

        ExpoApprovalResponseDto res = service.updateExpoStatus(1L, request("APPROVED", null));

        assertThat(res.status()).isEqualTo(ExpoStatus.APPROVED);
        assertThat(res.credential().loginId()).hasSize(8).isEqualTo(applicant.getCustomerId());
        assertThat(res.credential().temporaryPassword()).hasSize(12);
        assertThat(applicant.getPassword()).isEqualTo("bcrypt-hash");
        assertThat(expo.getExpoAdmin()).isSameAs(applicant);
    }

    @Test
    @DisplayName("사유 없이 반려하면 400 (기존: null 검사 순서 오류로 NPE 500)")
    void reject_requiresReason() {
        when(expoRepository.findById(1L)).thenReturn(Optional.of(pendingExpo("박람회")));

        assertError(() -> service.updateExpoStatus(1L, request("REJECTED", null)), CustomErrorCode.REASON_REQUIRED);
        assertError(() -> service.updateExpoStatus(1L, request("REJECTED", "  ")), CustomErrorCode.REASON_REQUIRED);
    }

    @Test
    @DisplayName("이미 처리된 박람회는 다시 처리할 수 없다")
    void alreadyHandled() {
        Expo approved = Expo.builder().expoId(1L).title("박람회").status(ExpoStatus.APPROVED).build();
        when(expoRepository.findById(1L)).thenReturn(Optional.of(approved));

        assertError(() -> service.updateExpoStatus(1L, request("REJECTED", "사유")), CustomErrorCode.HANDLED_EXPO);
    }

    @Test
    @DisplayName("담당 박람회가 있는 관리자는 삭제하지 않는다 (기존: FK 위반 500)")
    void delete_blockedWhenAdminHasExpos() {
        ExpoAdmin admin = ExpoAdmin.builder().expoAdminId(3L).build();
        when(expoAdminRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(expoRepository.existsByExpoAdmin(admin)).thenReturn(true);

        assertError(() -> service.deleteExpoAdmin(3L), CustomErrorCode.EXPO_ADMIN_HAS_EXPOS);
        verify(expoAdminRepository, never()).delete(any());
    }

    @Test
    @DisplayName("비밀번호를 재발급하면 기존 로그인 세션(리프레시 토큰)을 끊는다")
    void resetPassword_revokesSessions() {
        ExpoAdmin admin = ExpoAdmin.builder().expoAdminId(3L).customerId("expoadmin3").password("old").build();
        when(expoAdminRepository.findById(3L)).thenReturn(Optional.of(admin));
        when(expoRepository.existsByExpoAdmin(admin)).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("new-hash");

        var credential = service.resetExpoAdminPassword(3L);

        assertThat(credential.loginId()).isEqualTo("expoadmin3");
        assertThat(admin.getPassword()).isEqualTo("new-hash");
        verify(tokenStore).deleteRefreshToken(AccountType.EXPO_ADMIN, 3L);
    }

    private static Expo pendingExpo(String title) {
        return Expo.builder().expoId(1L).title(title).status(ExpoStatus.PENDING).build();
    }

    private static ExpoStatusRequestDto request(String status, String reason) {
        ExpoStatusRequestDto dto = new ExpoStatusRequestDto();
        ReflectionTestUtils.setField(dto, "status", status);
        ReflectionTestUtils.setField(dto, "reason", reason);
        return dto;
    }

    private static void assertError(org.assertj.core.api.ThrowableAssert.ThrowingCallable call, CustomErrorCode code) {
        assertThatThrownBy(call)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(code);
    }
}
