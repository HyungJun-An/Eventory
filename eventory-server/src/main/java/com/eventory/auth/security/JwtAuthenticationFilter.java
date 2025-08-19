package com.eventory.auth.security;

import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserRepository;
import com.eventory.auth.tokenStore.TokenStore;

import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoAdminRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

/**
 *  - 컨트롤러 진입 전에 토큰 검사/권한 심음
 *  - 주의: 블랙리스트(로그아웃 처리된 AccessToken)는 무조건 차단함
 *  - 1) 토큰 파싱 2) 블랙리스트 확인 3) role에 따라 엔티티 로드 4) SecurityContext 설정
 * */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenStore tokenStore;
    private final RedisTemplate<String, String> redisTemplate;

    private final UserRepository userRepository;
    private final ExpoAdminRepository expoAdminRepository;
    private final SystemAdminRepository systemAdminRepository;

    private static final String BLACKLIST_PREFIX = "blacklist:access:";
    private static final String[] WHITELIST = {
            // Auth & Admin login endpoints
            "/api/auth/login", "/api/auth/register", "/api/auth/refresh",
            "/api/admin/login", "/api/admin/sys/login",
            // Public resources & docs
            "/swagger-ui", "/swagger-resources", "/v3/api-docs",
            "/webjars", "/favicon.ico", "/error",
            // Public user APIs
            "/api/user/expos", "/api/user/expos/",
            // Others
            "/session", "/actuator"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String p = request.getRequestURI();

        return  p.equals("/v3/api-docs") || p.equals("/v3/api-docs.yaml")
                || p.startsWith("/swagger-ui") || p.startsWith("/swagger-resources")
                || p.startsWith("/webjars") || p.equals("/favicon.ico") || p.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 0) CORS preflight는 바로 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String uri = request.getRequestURI();
        // 1) 공개 경로는 필터 우회
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2) Authorization 헤더 없으면 그냥 통과 (permitAll 된 곳/비보호 자원 접근 허용)
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            // 3) 블랙리스트 토큰 거부 (로그아웃 처리된 토큰)
            if (redisTemplate.hasKey(BLACKLIST_PREFIX + token)) {
                unauthorized(response, "Logged-out token");
                return;
            }

            // 4) 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(token)) {
                unauthorized(response, "Invalid token");
                return;
            }

            // 5) 클레임에서 식별자/역할 추출
            // "generalUser" | "companyUser" | "expoAdmin" | "systemAdmin"
            String rawRole = jwtTokenProvider.getRoleFromToken(token);
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // ROLE_ 접두 들어오면 내부 키로 정규화
            String role = rawRole;
            if (role != null && role.startsWith("ROLE_")) {
                role = role.substring(5).toLowerCase(); // "SYSTEM_ADMIN" -> "system_admin"
                if (role.equals("system_admin")) role = "systemAdmin";
                else if (role.equals("expo_admin")) role = "expoAdmin";
                else if (role.equals("general_user") || role.equals("companyuser")) role = "generalUser";
            }

            // 6) 역할별로 해당 엔티티 로딩 → CustomUserPrincipal 구성 → SecurityContext에 심음
            UsernamePasswordAuthenticationToken authentication = switch (role) {
                case "generalUser", "companyUser" -> userRepository.findById(userId)
                        .map(u -> new UsernamePasswordAuthenticationToken(
                                // fromUser는 그대로 유지 (프록시 이슈가 있으면 오버로드 버전 사용)
                                CustomUserPrincipal.fromUser(u), null,
                                CustomUserPrincipal.authoritiesOf("ROLE_GENERAL_USER")))
                        .orElse(null);
                case "expoAdmin" -> expoAdminRepository.findById(userId)
                        .map(a -> new UsernamePasswordAuthenticationToken(
                                CustomUserPrincipal.fromExpoAdmin(a), null,
                                CustomUserPrincipal.authoritiesOf("ROLE_EXPO_ADMIN")))
                        .orElse(null);
                case "systemAdmin" -> systemAdminRepository.findById(userId)
                        .map(a -> new UsernamePasswordAuthenticationToken(
                                CustomUserPrincipal.fromSystemAdmin(a), null,
                                CustomUserPrincipal.authoritiesOf("ROLE_SYSTEM_ADMIN")))
                        .orElse(null);
                default -> null; // 미정의 역할 → 인증 안 심음
            };

            // 엔티티 없거나 역할 못 읽으면 인증 미설정 상태로 통과 → 보호 자원에서는 401/403 처리됨
            if (authentication != null) {
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            unauthorized(response, "Unauthorized token");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후 토큰
        }
        return null;
    }

    private void filterFilterChain(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(request, response);
    }

    private String normalizeRole(String raw) {
        if (raw == null) return "";
        return "companyUser".equalsIgnoreCase(raw) ? "generalUser" : raw; // 통일
    }

    private boolean isWhitelisted(String uri) {
        if (uri == null) return false;
        for (String open : WHITELIST) {
            if (uri.equals(open) || uri.startsWith(open)) {
                return true;
            }
        }
        return false;
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
