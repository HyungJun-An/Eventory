package com.eventory.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidityInMs;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidityInMs;

    private final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS256;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
    
    // 3,4 참가업체와 참관객 로그인시
    // AccessToken 생성 (userId를 subject로)
    public String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityInMs);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()), SIGNATURE_ALGORITHM)
                .compact();
    }

    // 관리자 토큰: role 클레임 포함
    public String createAccessTokenWithRole(Long adminId, String role){
        Date now = new Date();
        Date exp = new Date(now.getTime() + accessTokenValidityInMs);
        return Jwts.builder()
                .setSubject(String.valueOf(adminId))
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // RefreshToken 생성 (UUID 기반)
    public String createRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // 토큰에서 userId 추출
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
//                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            System.out.println("잘못된 JWT 서명입니다: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.out.println("JWT 토큰이 만료되었습니다: " + e.getMessage());
        }
        return false;
    }

    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료된 토큰도 claims 추출 가능
        }
    }

    // 토큰 남은 시간 계산
    /**
     * 접근 토큰의 남은 유효시간(ms)을 반환함.
     * - 만료되었거나 토큰이 잘못되었으면 0을 반환함(음수 방지로 0 하한 고정).
     * - StandardCharsets.UTF_8 인코딩으로 시크릿 키 바이트 변환.
     * 운영 들어가면 secretKey 길이/알고리즘, JJWT 버전 호환(0.11.x)만 확인하면 됨
     */
    public long getRemainingValidity(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return 0L;
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            long remaining = claims.getExpiration().getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0L); // 만약 시스템 시계 이슈 등으로 음수가 나오면 0으로 보정

            // 토큰이 이미 만료된 경우 ExpiredJwtException이 터지기 때문에 예외 잡아줘야 함
        } catch (ExpiredJwtException e) {
            // 이미 만료됨
            return 0L;
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 포맷 오류 등 모든 잘못된 토큰
            return 0L;
        }
    }
}
