package com.modeunsa.global.security.jwt;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  private final JwtProperties jwtProperties;
  private static final String KEY_ROLE = "role";
  private static final String KEY_TYPE = "type";
  private static final String TYPE_ACCESS = "access";
  private static final String TYPE_REFRESH = "refresh";


  private SecretKey secretKey;

  @PostConstruct
  public void init() {
    byte[] keyBytes = jwtProperties.secret().getBytes();

    if (keyBytes.length < 32) {
      throw new GeneralException(ErrorStatus.CONFIG_INVALID_JWT_SECRET);
    }

    this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    log.info("JWT SecretKey initialized successfully");
  }

  /**
   * Access Token 생성
   */
  public String createAccessToken(Long memberId, MemberRole role) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

    return Jwts.builder()
        .subject(String.valueOf(memberId))
        .claim(KEY_ROLE, role.name())
        .claim(KEY_TYPE, TYPE_ACCESS)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Refresh Token 생성
   */
  public String createRefreshToken(Long memberId, MemberRole role) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

    return Jwts.builder()
        .subject(String.valueOf(memberId))
        .claim(KEY_ROLE, role.name())
        .claim(KEY_TYPE, TYPE_REFRESH)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
  }

  /**
   * Refresh Token 여부 확인
   */
  public boolean isRefreshToken(String token) {
    Claims claims = parseClaims(token);
    return TYPE_REFRESH.equals(claims.get(KEY_TYPE, String.class));
  }

  /**
   * Access Token 여부 확인
   */
  public boolean isAccessToken(String token) {
    Claims claims = parseClaims(token);
    return TYPE_ACCESS.equals(claims.get(KEY_TYPE, String.class));
  }

  /**
   * 토큰 유효성 검증
   */
  public void validateTokenOrThrow(String token) {
    if (!StringUtils.hasText(token)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }

    try {
      parseClaims(token);
    } catch (ExpiredJwtException e) {
      throw new GeneralException(ErrorStatus.AUTH_EXPIRED_TOKEN);
    } catch (MalformedJwtException e) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }
  }

  /**
   * 토큰에서 memberId 추출
   */
  public Long getMemberIdFromToken(String token) {
    Claims claims = parseClaims(token);
    return Long.parseLong(claims.getSubject());
  }

  /**
   * 토큰에서 role 추출
   */
  public MemberRole getRoleFromToken(String token) {
    Claims claims = parseClaims(token);
    return MemberRole.valueOf(claims.get(KEY_ROLE, String.class));
  }

  /**
   * Claims 파싱
   */
  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public long getAccessTokenExpiration() {
    return jwtProperties.accessTokenExpiration();
  }

  public long getRefreshTokenExpiration() {
    return jwtProperties.refreshTokenExpiration();
  }
}