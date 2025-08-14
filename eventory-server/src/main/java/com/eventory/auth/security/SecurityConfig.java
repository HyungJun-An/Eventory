package com.eventory.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
                                "/api/admin/login", "/api/admin/sys/login",
                                "/api/auth/login", "/api/auth/signup", "/api/auth/refresh",
                                "/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**",
                                "/webjars/**", "/favicon.ico", "/error",
                                "/api/user/expos", "/api/user/expos/**",
                                "/session/**", "/actuator/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/me").authenticated() // me는 인증만 필요
                        // 로그아웃은 인증 필요 (화이트리스트/permitAll 금지)
                        .requestMatchers("/api/admin/sys/logout").authenticated()
                        .requestMatchers("/api/admin/logout").authenticated()
                        // 관리자 전용 도메인
                        .requestMatchers("/api/sys/expo/**").hasRole("SYSTEM_ADMIN") 
                        .requestMatchers("/api/admin/expo/**").hasRole("EXPO_ADMIN")
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
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true); // 크리덴셜 허용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
