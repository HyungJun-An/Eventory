package com.eventory.auth.service;

import com.eventory.auth.dto.LoginResponse;
import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserTypeRepository;
import com.eventory.auth.security.JwtTokenProvider;
import com.eventory.auth.tokenStore.AccountType;
import com.eventory.auth.tokenStore.RefreshTokenOwner;
import com.eventory.auth.tokenStore.TokenStore;
import com.eventory.common.entity.SystemAdmin;
import com.eventory.common.entity.UserType;
import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoAdminRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 리프레시 토큰 권한 상승 회귀 테스트.
 * user / expo_admin / system_admin 테이블의 id 는 서로 겹친다 (모두 1부터 시작).
 * 수정 전에는 참관객(user_id=1)의 리프레시 토큰으로 system_admin_id=1 의 ROLE_SYSTEM_ADMIN 토큰이 발급됐다.
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthServiceImplTest {

    private static final String REFRESH = "refresh-uuid";

    @Mock SystemAdminRepository systemAdminRepository;
    @Mock ExpoAdminRepository expoAdminRepository;
    @Mock UserTypeRepository userTypeRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock TokenStore tokenStore;

    @InjectMocks AdminAuthServiceImpl service;

    @ParameterizedTest(name = "{0} 토큰으로는 시스템관리자 토큰을 받을 수 없다")
    @EnumSource(value = AccountType.class, names = {"USER", "EXPO_ADMIN"})
    void refreshSystemAdmin_rejectsOtherAccountTypes(AccountType type) {
        when(tokenStore.findOwner(REFRESH)).thenReturn(Optional.of(new RefreshTokenOwner(type, 1L)));

        assertInvalidRefresh(() -> service.refreshSystemAdmin(REFRESH));
        verifyNoInteractions(systemAdminRepository, jwtTokenProvider);
    }

    @ParameterizedTest(name = "{0} 토큰으로는 박람회관리자 토큰을 받을 수 없다")
    @EnumSource(value = AccountType.class, names = {"USER", "SYSTEM_ADMIN"})
    void refreshExpoAdmin_rejectsOtherAccountTypes(AccountType type) {
        when(tokenStore.findOwner(REFRESH)).thenReturn(Optional.of(new RefreshTokenOwner(type, 1L)));

        assertInvalidRefresh(() -> service.refreshExpoAdmin(REFRESH));
        verifyNoInteractions(expoAdminRepository, jwtTokenProvider);
    }

    @Test
    @DisplayName("폐기·만료된 리프레시 토큰은 거부한다")
    void revokedToken() {
        when(tokenStore.findOwner(REFRESH)).thenReturn(Optional.empty());

        assertInvalidRefresh(() -> service.refreshSystemAdmin(REFRESH));
    }

    @Test
    @DisplayName("시스템관리자 토큰이면 새 토큰을 발급하고 리프레시 토큰을 교체한다")
    void refreshSystemAdmin_success() {
        UserType systemAdminType = new UserType(1L, "SYSTEM_ADMIN");
        when(tokenStore.findOwner(REFRESH)).thenReturn(Optional.of(new RefreshTokenOwner(AccountType.SYSTEM_ADMIN, 1L)));
        when(systemAdminRepository.findById(1L)).thenReturn(Optional.of(
                new SystemAdmin(1L, systemAdminType, "sysadmin", "hash", "운영자", "ops@example.com", "010-0000-0000")));
        when(userTypeRepository.findById(1L)).thenReturn(Optional.of(systemAdminType));
        when(jwtTokenProvider.createAccessTokenWithRole(1L, "ROLE_SYSTEM_ADMIN")).thenReturn("new-access");

        LoginResponse res = service.refreshSystemAdmin(REFRESH);

        assertThat(res.getAccessToken()).isEqualTo("new-access");
        assertThat(res.getRefreshToken()).isNotEqualTo(REFRESH);
        verify(tokenStore).saveRefreshToken(eq(AccountType.SYSTEM_ADMIN), eq(1L), eq(res.getRefreshToken()), anyLong());
    }

    private static void assertInvalidRefresh(org.assertj.core.api.ThrowableAssert.ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(CustomException.class)
                .extracting(e -> ((CustomException) e).getErrorCode())
                .isEqualTo(CustomErrorCode.INVALID_REFRESH_TOKEN);
    }
}
