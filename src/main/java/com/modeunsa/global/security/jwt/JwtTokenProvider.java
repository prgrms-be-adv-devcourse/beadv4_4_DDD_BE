package com.modeunsa.global.security.jwt;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
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
  private static final String KEY_SELLER_ID = "sellerId";

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

  /** Access Token 생성 */
  public String createAccessToken(Long memberId, MemberRole role, Long sellerId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiration());

    return Jwts.builder()
        .subject(String.valueOf(memberId))
        .claim(KEY_ROLE, role.name())
        .claim(KEY_TYPE, TYPE_ACCESS)
        .claim(KEY_SELLER_ID, sellerId)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
  }

  /** Refresh Token 생성 */
  public String createRefreshToken(Long memberId, MemberRole role, Long sellerId) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtProperties.refreshTokenExpiration());

    return Jwts.builder()
        .subject(String.valueOf(memberId))
        .claim(KEY_ROLE, role.name())
        .claim(KEY_TYPE, TYPE_REFRESH)
        .claim(KEY_SELLER_ID, sellerId)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(secretKey)
        .compact();
  }

  /** Refresh Token 여부 확인 */
  public boolean isRefreshToken(String token) {
    Claims claims = parseClaims(token);
    return TYPE_REFRESH.equals(claims.get(KEY_TYPE, String.class));
  }

  /** Access Token 여부 확인 */
  public boolean isAccessToken(String token) {
    Claims claims = parseClaims(token);
    return TYPE_ACCESS.equals(claims.get(KEY_TYPE, String.class));
  }

  /** 토큰 유효성 검증 */
  public void validateTokenOrThrow(String token) {
    if (!StringUtils.hasText(token)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }

    try {
      parseClaims(token);
    } catch (ExpiredJwtException e) {
      throw new GeneralException(ErrorStatus.AUTH_EXPIRED_TOKEN);
    } catch (Exception e) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }
  }

  /** 토큰에서 memberId 추출 */
  public Long getMemberIdFromToken(String token) {
    Claims claims = parseClaims(token);
    String subject = claims.getSubject();

    if (!StringUtils.hasText(subject)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }

    try {
      return Long.parseLong(subject);
    } catch (NumberFormatException e) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }
  }

  /** 토큰에서 role 추출 */
  public MemberRole getRoleFromToken(String token) {
    Claims claims = parseClaims(token);
    String roleStr = claims.get(KEY_ROLE, String.class);

    if (!StringUtils.hasText(roleStr)) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }

    try {
      return MemberRole.valueOf(roleStr);
    } catch (IllegalArgumentException e) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN);
    }
  }

  /** 토큰에서 sellerId 추출 */
  public Long getSellerIdFromToken(String token) {
    Claims claims = parseClaims(token);
    Object sellerIdObj = claims.get(KEY_SELLER_ID);

    if (sellerIdObj == null) {
      return null;
    }

    return Long.valueOf(String.valueOf(sellerIdObj));
  }

  /** Claims 파싱 */
  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }

  public long getAccessTokenExpiration() {
    return jwtProperties.accessTokenExpiration();
  }

  public long getRefreshTokenExpiration() {
    return jwtProperties.refreshTokenExpiration();
  }

  /** 토큰의 남은 만료시간 계산 (밀리초) */
  public long getRemainingExpiration(String token) {
    try {
      Claims claims = parseClaims(token);
      Date expiration = claims.getExpiration();
      long remainingTime = expiration.getTime() - System.currentTimeMillis();
      return Math.max(0, remainingTime);
    } catch (ExpiredJwtException e) {
      // 만료된 토큰은 블랙리스트에 등록할 필요 없으므로 0 반환
      log.debug("이미 만료된 토큰 - 블랙리스트 등록 불필요");
      return 0;
    }
  }
}
