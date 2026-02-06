package com.modeunsa.global.security.jwt;

import com.modeunsa.boundedcontext.auth.app.usecase.AuthTokenReissueUseCase;
import com.modeunsa.boundedcontext.auth.out.repository.AuthAccessTokenBlacklistRepository;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.config.CookieProperties;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final AuthAccessTokenBlacklistRepository blacklistRepository;
  private final AuthTokenReissueUseCase authTokenReissueUseCase;
  private final CookieProperties cookieProperties;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ROLE_PREFIX = "ROLE_";
  private static final String EXCEPTION_ATTRIBUTE = "exception";

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return request.getRequestURI().contains("/internal/");
  }

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String token = resolveAccessToken(request);

    if (StringUtils.hasText(token)) {
      try {
        jwtTokenProvider.validateTokenOrThrow(token);

        if (!jwtTokenProvider.isAccessToken(token)) {
          throw new GeneralException(ErrorStatus.AUTH_INVALID_ACCESS_TOKEN);
        }

        // 블랙리스트 체크
        if (blacklistRepository.existsById(token)) {
          throw new GeneralException(ErrorStatus.AUTH_BLACKLISTED_TOKEN);
        }

        // 인증 처리
        setAuthentication(token, request);

      } catch (GeneralException e) {
        // 토큰 만료 시 자동 재발급 시도
        if (e.getErrorStatus() == ErrorStatus.AUTH_EXPIRED_TOKEN) {
          String refreshToken = resolveRefreshToken(request);

          if (StringUtils.hasText(refreshToken)) {
            try {
              // 재발급 시도
              JwtTokenResponse newTokens = attemptTokenRefresh(refreshToken);

              // 새 토큰으로 인증 설정
              setAuthentication(newTokens.accessToken(), request);

              // 응답 헤더에 새 쿠키 설정
              setNewTokenCookies(response, newTokens);

              log.info("토큰 자동 재발급 성공 - memberId: {}",
                  jwtTokenProvider.getMemberIdFromToken(newTokens.accessToken()));

              filterChain.doFilter(request, response);
              return;

            } catch (Exception refreshError) {
              log.warn("토큰 자동 재발급 실패: {}", refreshError.getMessage());
              request.setAttribute(EXCEPTION_ATTRIBUTE, e);
            }
          }
        }

        log.warn("토큰 검증 실패: {}", e.getMessage());
        request.setAttribute(EXCEPTION_ATTRIBUTE, e);
      }
    }

    filterChain.doFilter(request, response);
  }

  /** Access Token 추출 */
  private String resolveAccessToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

    // 1. 기존 헤더 방식 유지
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }

    // 2. 쿠키에서 accessToken 추출
    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("accessToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

  /** Refresh Token 추출 */
  private String resolveRefreshToken(HttpServletRequest request) {
    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("refreshToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

  /** 인증 정보 설정 */
  private void setAuthentication(String token, HttpServletRequest request) {
    Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
    MemberRole role = jwtTokenProvider.getRoleFromToken(token);
    Long sellerId = jwtTokenProvider.getSellerIdFromToken(token);

    CustomUserDetails principal = new CustomUserDetails(memberId, role, sellerId);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            principal, null, List.of(new SimpleGrantedAuthority(ROLE_PREFIX + role.name())));

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);

    log.debug("인증 정보 저장 완료 - memberId: {}, role: {}", memberId, role);
  }

  /** 토큰 재발급 시도 */
  private JwtTokenResponse attemptTokenRefresh(String refreshToken) {
    return authTokenReissueUseCase.execute(refreshToken);
  }

  /** 새 토큰 쿠키 설정 */
  private void setNewTokenCookies(HttpServletResponse response, JwtTokenResponse tokens) {
    // Access Token 쿠키
    ResponseCookie accessTokenCookie =
        ResponseCookie.from("accessToken", tokens.accessToken())
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(tokens.accessTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    // Refresh Token 쿠키
    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", tokens.refreshToken())
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(tokens.refreshTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());
  }
}