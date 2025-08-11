package com.eventory.auth.security;

import com.eventory.auth.repository.UserRepository;
import com.eventory.auth.tokenStore.TokenStore;

import com.eventory.common.exception.CustomErrorCode;
import com.eventory.common.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenStore tokenStore;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        return path.equals("/v3/api-docs") ||
                path.equals("/v3/api-docs.yaml") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars") ||
                path.equals("/favicon.ico") ||
                path.equals("/error");
    }

//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response,
//                                    FilterChain filterChain) throws ServletException, IOException {
//        // 요청 헤더에서 토큰 추출
//        String token = resolveToken(request);
//
//        // 토큰이 있고 유효한 경우에만 처리
//        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
//
//            // 1. 블랙리스트에 등록된 토큰인지 확인
//            String isBlacklisted = redisTemplate.opsForValue().get("blacklist:" + token);
//            if (isBlacklisted != null) {
//                // 블랙리스트 토큰일 경우 예외 발생
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                throw new CustomException(CustomErrorCode.LOGGED_OUT_TOKEN);
//            }
//
//            // 2. 유저 조회 (userId 기준)
//            Long userId = jwtTokenProvider.getUserIdFromToken(token); // 토큰에서 userId 추출
//            // userId로 유저 조회 및 인증 객체 생성
//            userRepository.findById(userId).ifPresent(user -> {
//                UsernamePasswordAuthenticationToken authentication =
//                        new UsernamePasswordAuthenticationToken(user, null, null);
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authentication);
//            });
//        }
//        // 다음 필터로 요청 전달
//        filterChain.doFilter(request, response);
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            // [추가] 로그아웃된(블랙리스트) 토큰 거부
            if (tokenStore.isBlacklisted(token)) {
                throw new CustomException(CustomErrorCode.LOGGED_OUT_TOKEN); // "해당 토큰은 로그아웃 처리된 토큰입니다"
            }
            // 기존: 유효성 검사 + SecurityContext setAuthentication(...)
//            jwtTokenProvider.authenticate(token);
            Long userId = jwtTokenProvider.getUserIdFromToken(token); // 토큰에서 userId 추출
            // userId로 유저 조회 및 인증 객체 생성
            userRepository.findById(userId).ifPresent(user -> {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후 토큰
        }
        return null;
    }
}
