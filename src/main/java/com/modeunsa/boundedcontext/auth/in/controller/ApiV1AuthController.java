package com.modeunsa.boundedcontext.auth.in.controller;

import com.modeunsa.boundedcontext.auth.app.facade.AuthFacade;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.auth.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
public class ApiV1AuthController {

  private static final String BEARER_PREFIX = "Bearer ";

  private final AuthFacade authFacade;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(summary = "OAuth2 로그인 URL 조회", description = "OAuth2 로그인 URL을 반환합니다.")
  @GetMapping("/oauth/{provider}/url")
  public ResponseEntity<ApiResponse> getOAuthLoginUrl(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "리다이렉트 URI") @RequestParam(required = false) String redirectUri) {
    OAuthProvider oauthProvider = findProvider(provider);
    //     TODO: 테스트를 위해 임시로 토큰 발급 로직으로 대체 -> 실제로는 아래 주석 처리된 코드 사용
    //    String loginUrl = authFacade.getOAuthLoginUrl(oauthProvider, redirectUri);
    TokenResponse tokenResponse = authFacade.login(1L, MemberRole.MEMBER);
    //    return ApiResponse.onSuccess(SuccessStatus.OK, loginUrl);
    return ApiResponse.onSuccess(SuccessStatus.AUTH_LOGIN_SUCCESS, tokenResponse);
  }

  @Operation(summary = "소셜 로그인", description = "소셜 로그인 인증 코드를 사용하여 로그인 및 토큰을 발급합니다.")
  @PostMapping("/login/{provider}")
  public ResponseEntity<ApiResponse> login(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "인증 코드", required = true) @RequestParam String code,
      @Parameter(description = "리다이렉트 URI") @RequestParam(required = false) String redirectUri) {

    OAuthProvider oauthProvider = findProvider(provider);
//    TODO: 테스트를 위해 임시로 토큰 발급 로직으로 대체 -> 실제로는 아래 주석 처리된 코드 사용
//    TokenResponse tokenResponse = authFacade.oauthLogin(oauthProvider, code, redirectUri);
    TokenResponse tokenResponse = authFacade.login(1L, MemberRole.MEMBER);

    return ApiResponse.onSuccess(SuccessStatus.AUTH_LOGIN_SUCCESS, tokenResponse);
  }

  @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 Access Token을 재발급합니다.")
  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse> reissue(
      @Parameter(description = "Refresh Token", required = true) @RequestHeader("RefreshToken")
          String refreshToken) {
    // TODO: 실제로는 Refresh Token 내부의 정보나 DB 조회를 통해 Role을 가져와야 할 수 있습니다.
    TokenResponse tokenResponse = authFacade.reissueToken(refreshToken);

    return ApiResponse.onSuccess(SuccessStatus.AUTH_TOKEN_REFRESH_SUCCESS, tokenResponse);
  }

  @Operation(summary = "로그아웃", description = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제합니다.")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse> logout(
      @Parameter(description = "Access Token", required = true) @RequestHeader("Authorization")
          String authorizationHeader) {

    // 1. Bearer 토큰 추출
    String accessToken = resolveToken(authorizationHeader);
    if (accessToken == null) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN_FORMAT);
    }

    // 2. 토큰 유효성 검증
    jwtTokenProvider.validateTokenOrThrow(accessToken);

    // 3. Access Token인지 확인
    if (!jwtTokenProvider.isAccessToken(accessToken)) {
      throw new GeneralException(ErrorStatus.AUTH_NOT_ACCESS_TOKEN);
    }
    authFacade.logout(accessToken);

    return ApiResponse.onSuccess(SuccessStatus.AUTH_LOGOUT_SUCCESS);
  }

  @Operation(summary = "[테스트용] 인증 확인", description = "Access Token 유효성을 확인하고 인증된 사용자 정보를 반환합니다.")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse> checkAuth() {
    // SecurityContext에서 인증 정보 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new GeneralException(ErrorStatus.AUTH_UNAUTHORIZED);
    }

    // 인증된 사용자 정보 반환
    Map<String, Object> userInfo =
        Map.of(
            "memberId", authentication.getName(),
            "authorities", authentication.getAuthorities());

    return ApiResponse.onSuccess(SuccessStatus.OK, userInfo);
  }

  /**
   * Authorization 헤더에서 Bearer 토큰 추출
   *
   * @param bearerToken Authorization 헤더 값
   * @return 추출된 토큰, 형식이 잘못되면 null
   */
  private String resolveToken(String bearerToken) {
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  /** Provider 문자열 -> Enum 변환 */
  private OAuthProvider findProvider(String providerName) {
    return Arrays.stream(OAuthProvider.values())
        .filter(p -> p.name().equalsIgnoreCase(providerName))
        .findFirst()
        .orElseThrow(() -> new GeneralException(ErrorStatus.OAUTH_INVALID_PROVIDER));
  }
}
