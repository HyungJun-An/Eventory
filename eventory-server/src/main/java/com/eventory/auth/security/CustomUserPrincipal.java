package com.eventory.auth.security;

import com.eventory.common.entity.ExpoAdmin;
import com.eventory.common.entity.SystemAdmin;
import com.eventory.common.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class CustomUserPrincipal implements UserDetails {
    private Long id;
    private String username; // customerId 등 UI 표기용
    private String role;  // "generalUser", "companyUser", "expoAdmin", "systemAdmin"
    private Collection<? extends GrantedAuthority> authorities;

    public static CustomUserPrincipal fromUser(User user) {
        return new CustomUserPrincipal(
                user.getUserId(),
                user.getCustomerId(),
                "ROLE_GENERAL_USER",
                authoritiesOf("ROLE_GENERAL_USER")
        );
    }

    public static CustomUserPrincipal fromExpoAdmin(ExpoAdmin admin) {
        return new CustomUserPrincipal(
                admin.getExpoAdminId(),
                admin.getCustomerId(),
                "ROLE_EXPO_ADMIN",
                authoritiesOf("ROLE_EXPO_ADMIN")
        );
    }

    public static CustomUserPrincipal fromSystemAdmin(SystemAdmin admin) {
        return new CustomUserPrincipal(
                admin.getSystemAdminId(),
                admin.getCustomerId(),
                "ROLE_SYSTEM_ADMIN",
                authoritiesOf("ROLE_SYSTEM_ADMIN")
        );
    }

    public static List<GrantedAuthority> authoritiesOf(String role) {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return null; } // JWT 기반이라 여기선 미사용
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

