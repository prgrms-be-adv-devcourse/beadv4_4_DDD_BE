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
import java.util.Arrays;
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

  // PRE_ACTIVE 상태일 때도 접근 가능한 URL 목록 (Whitelist)
  private static final String[] PRE_ACTIVE_ALLOW_URLS = {
      "/api/v1/auths",                     // 로그아웃, 토큰 재발급 등
      "/api/v1/members/me/basic-info",     // 기본 정보 조회
      "/api/v2/members/me/signup-complete",// 가입 완료 처리
      "/api/v1/files"                      // 이미지 업로드
  };

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
        // 1. 토큰 검증
        jwtTokenProvider.validateTokenOrThrow(token);

        if (!jwtTokenProvider.isAccessToken(token)) {
          throw new GeneralException(ErrorStatus.AUTH_INVALID_ACCESS_TOKEN);
        }

        if (blacklistRepository.existsById(token)) {
          throw new GeneralException(ErrorStatus.AUTH_BLACKLISTED_TOKEN);
        }

        // 2. 상태 기반 접근 제어 (PRE_ACTIVE 체크)
        String status = jwtTokenProvider.getStatusFromToken(token);
        if ("PRE_ACTIVE".equals(status)) {
          if (!isAllowedForPreActive(request.getRequestURI())) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_ACTIVATED);
          }
        }

        // 3. 인증 객체 설정
        setAuthentication(token, request);

      } catch (GeneralException e) {
        // Auth 엔드포인트는 자동 재발급 제외
        if (e.getErrorStatus() == ErrorStatus.AUTH_EXPIRED_TOKEN && !isAuthEndpoint(request)) {

          String refreshToken = resolveRefreshToken(request);

          if (StringUtils.hasText(refreshToken)) {
            try {
              JwtTokenResponse newTokens = attemptTokenRefresh(refreshToken);
              setAuthentication(newTokens.accessToken(), request);
              setNewTokenCookies(response, newTokens);

              log.info(
                  "토큰 자동 재발급 성공 - memberId: {}",
                  jwtTokenProvider.getMemberIdFromToken(newTokens.accessToken()));

              filterChain.doFilter(request, response);
              return;

            } catch (GeneralException refreshError) {
              // 재발급 실패 시 구체적인 에러 저장
              log.warn(
                  "토큰 자동 재발급 실패 - {}: {}",
                  refreshError.getErrorStatus().getCode(),
                  refreshError.getMessage());
              request.setAttribute(EXCEPTION_ATTRIBUTE, refreshError);
              filterChain.doFilter(request, response);
              return;

            } catch (Exception refreshError) {
              // 예상치 못한 에러
              log.error("토큰 재발급 중 예외 발생: {}", refreshError.getMessage());
              request.setAttribute(
                  EXCEPTION_ATTRIBUTE, new GeneralException(ErrorStatus.AUTH_TOKEN_REFRESH_FAILED));
              filterChain.doFilter(request, response);
              return;
            }
          }
        }

        log.warn("토큰 검증 실패: {}", e.getMessage());
        request.setAttribute(EXCEPTION_ATTRIBUTE, e);
      }
    }

    filterChain.doFilter(request, response);
  }

  /** Auth 관련 엔드포인트 체크 */
  private boolean isAuthEndpoint(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/api/v1/auths/");
  }

  /** PRE_ACTIVE 상태 허용 URL 체크 */
  private boolean isAllowedForPreActive(String requestUri) {
    // 1. Auth 관련은 무조건 허용 (isAuthEndpoint 로직 재활용 또는 포함)
    if (requestUri.startsWith("/api/v1/auths/")) return true;

    // 2. 화이트리스트 체크
    return Arrays.stream(PRE_ACTIVE_ALLOW_URLS)
        .anyMatch(requestUri::startsWith);
  }

  private String resolveAccessToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }

    if (request.getCookies() != null) {
      for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
        if ("accessToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }

    return null;
  }

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

  private JwtTokenResponse attemptTokenRefresh(String refreshToken) {
    return authTokenReissueUseCase.execute(refreshToken);
  }

  private void setNewTokenCookies(HttpServletResponse response, JwtTokenResponse tokens) {
    ResponseCookie accessTokenCookie =
        ResponseCookie.from("accessToken", tokens.accessToken())
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(tokens.accessTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

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
