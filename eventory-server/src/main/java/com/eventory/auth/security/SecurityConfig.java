package com.eventory.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                // 세션을 사용하지 않음 (JWT는 서버에 사용자 상태를 저장하지 않음 → 무상태 Stateless)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/admin/login", "/api/admin/sys/login", "/api/auth/login",
                                "/api/admin/logout", "/api/admin/sys/logout", "/api/auth/logout",
                                "/api/auth/signup", "/api/admin/refresh", "/api/admin/sys/refresh", "/api/auth/refresh",
                                "/swagger-ui.html", "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**",
                                "/webjars/**", "/favicon.ico", "/error", "/api/checkin/**",
                                "/api/user/expos", "/api/user/expos/**",
                                "/session/**", "/actuator/**"
                        ).permitAll()
                        // PortOne 웹훅은 PortOne 서버가 호출하므로 공개
                        .requestMatchers("/api/portone-webhook").permitAll()
                        // 결제·환불은 로그인한 참관객·참가업체만 (기존: 전부 공개 → 비로그인 결제 확정·타인 예약 환불 가능)
                        .requestMatchers("/api/payment/**").hasAnyRole("GENERAL_USER", "COMPANY_USER")
                        // 박람회 신청 엔드포인트 POST는 공개 허용
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/admin/expos").permitAll()
                        .requestMatchers("/api/auth/me").authenticated() // me는 인증만 필요
                        // 관리자 전용 도메인
                        // 시스템관리자 API (기존: 전부 공개 → 누구나 박람회 승인·관리자 삭제 가능)
                        .requestMatchers("/api/sys/**").hasRole("SYSTEM_ADMIN")
//                        .requestMatchers("/api/admin/expo/**").hasRole("EXPO_ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("EXPO_ADMIN")
                        // 나머지 전부 보호
                        .anyRequest().authenticated()
                )
                // JWT 필터 등록
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://localhost:8080",
                "https://localhost",
                "https://eventory.kro.kr",      // 포트 없이 도메인만
                "https://eventory.kro.kr:8080"  // 명시적으로 포트 포함
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 크리덴셜 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
