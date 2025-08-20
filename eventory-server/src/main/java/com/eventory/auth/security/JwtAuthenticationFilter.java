package com.eventory.auth.security;

import com.eventory.auth.repository.SystemAdminRepository;
import com.eventory.auth.repository.UserRepository;
import com.eventory.auth.tokenStore.TokenStore;

import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import com.eventory.common.repository.ExpoAdminRepository;
import io.jsonwebtoken.ExpiredJwtException;
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
            "/api/auth/login", "/api/auth/register",
            "/api/auth/refresh", "/api/admin/refresh",
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
        // 0) Refresh 엔드포인트는 UUID 헤더를 사용하므로 필터 스킵
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/auth/refresh")
                || uri.startsWith("/api/admin/refresh")
                || uri.startsWith("/api/admin/sys/refresh")) {
            filterChain.doFilter(request, response);
            return; // UUID를 JWT로 파싱하지 않음
        }


// 1) CORS preflight는 바로 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }


// 2) Authorization 헤더 없으면 그냥 통과 (permitAll 자원 접근 허용)
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }


// 3) Bearer 토큰 추출 및 형식 가드(JWT는 점 2개 포함)
        String token = header.substring(7);
        int p1 = token.indexOf('.');
        int p2 = (p1 < 0) ? -1 : token.indexOf('.', p1 + 1);
        if (p1 < 0 || p2 < 0) {
            unauthorized(response, "Invalid token format");
            return;
        }


        try {
// 4) 블랙리스트 토큰(로그아웃 처리된 토큰) 거부
            if (redisTemplate.hasKey(BLACKLIST_PREFIX + token)) {
                unauthorized(response, "Logged-out token");
                return;
            }


// 5) 토큰 유효성 검증
            if (!jwtTokenProvider.validateToken(token)) {
                unauthorized(response, "Invalid token");
                return;
            }


// 6) 클레임에서 식별자/역할 추출
            String role = jwtTokenProvider.getRoleFromToken(token); // 예: ROLE_EXPO_ADMIN
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            System.out.println("⭐ Jwt필터 " + role);


// 7) 역할별 엔티티 로딩 → CustomUserPrincipal 구성 → SecurityContext에 설정
            UsernamePasswordAuthenticationToken authentication = null;
            switch (role) {
                case "ROLE_GENERAL_USER":
                    authentication = userRepository.findById(userId)
                            .map(u -> new UsernamePasswordAuthenticationToken(
                                    CustomUserPrincipal.fromUser(u), null,
                                    CustomUserPrincipal.authoritiesOf("ROLE_GENERAL_USER")))
                            .orElse(null);
                    break;
                case "ROLE_EXPO_ADMIN":
                    authentication = expoAdminRepository.findById(userId)
                            .map(a -> new UsernamePasswordAuthenticationToken(
                                    CustomUserPrincipal.fromExpoAdmin(a), null,
                                    CustomUserPrincipal.authoritiesOf("ROLE_EXPO_ADMIN")))
                            .orElse(null);
                    break;
                case "ROLE_SYSTEM_ADMIN":
                    authentication = systemAdminRepository.findById(userId)
                            .map(s -> new UsernamePasswordAuthenticationToken(
                                    CustomUserPrincipal.fromSystemAdmin(s), null,
                                    CustomUserPrincipal.authoritiesOf("ROLE_SYSTEM_ADMIN")))
                            .orElse(null);
                    break;
                default:
                    authentication = null; // 알 수 없는 권한이면 인증 미설정
            }


            if (authentication != null) {
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }


// 8) 다음 필터로 진행
            filterChain.doFilter(request, response);


        } catch (ExpiredJwtException e) {
            unauthorized(response, "Expired token");
        } catch (Exception e) {
            unauthorized(response, "Unauthorized token");
        }
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
