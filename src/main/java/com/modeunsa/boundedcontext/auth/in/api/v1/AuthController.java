package com.modeunsa.boundedcontext.auth.in.api.v1;

import com.modeunsa.boundedcontext.auth.app.facade.AuthFacade;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.in.util.AuthRequestUtils;
import com.modeunsa.global.config.CookieProperties;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.auth.dto.AuthStatusResponse;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auths")
@RequiredArgsConstructor
public class AuthController {

  private final AuthFacade authFacade;
  private final CookieProperties cookieProperties;

  @Operation(summary = "OAuth2 로그인 URL 조회", description = "OAuth2 로그인 URL을 반환합니다.")
  @GetMapping("/oauth/{provider}/url")
  public ResponseEntity<ApiResponse> getOAuthLoginUrl(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "리다이렉트 URI") @RequestParam(required = false) String redirectUri) {
    OAuthProvider oauthProvider = AuthRequestUtils.findProvider(provider);
    String loginUrl = authFacade.getOAuthLoginUrl(oauthProvider, redirectUri);
    return ApiResponse.onSuccess(SuccessStatus.OK, loginUrl);
  }

  @Operation(summary = "소셜 로그인", description = "소셜 로그인 인증 코드를 사용하여 로그인 및 토큰을 발급합니다.")
  @PostMapping("/login/{provider}")
  public ResponseEntity<ApiResponse> login(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "인증 코드", required = true) @RequestParam String code,
      @Parameter(description = "리다이렉트 URI") @RequestParam(required = false) String redirectUri,
      @Parameter(description = "state 값", required = true) @RequestParam String state) {

    OAuthProvider oauthProvider = AuthRequestUtils.findProvider(provider);
    JwtTokenResponse jwtTokenResponse =
        authFacade.oauthLogin(oauthProvider, code, redirectUri, state);

    // Access Token 쿠키
    ResponseCookie accessTokenCookie =
        ResponseCookie.from("accessToken", jwtTokenResponse.accessToken())
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(jwtTokenResponse.accessTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    // Refresh Token 쿠키
    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", jwtTokenResponse.refreshToken())
            .httpOnly(true) // XSS 방어를 위해 반드시 true
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(jwtTokenResponse.refreshTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .body(ApiResponse.onSuccess(SuccessStatus.AUTH_LOGIN_SUCCESS, jwtTokenResponse).getBody());
  }

  @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 Access Token을 재발급합니다.")
  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse> reissue(
      @CookieValue(value = "refreshToken") String refreshToken) {
    JwtTokenResponse jwtTokenResponse = authFacade.reissueToken(refreshToken);

    ResponseCookie accessTokenCookie =
        ResponseCookie.from("accessToken", jwtTokenResponse.accessToken())
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(jwtTokenResponse.accessTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", jwtTokenResponse.refreshToken())
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(jwtTokenResponse.refreshTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .body(
            ApiResponse.onSuccess(SuccessStatus.AUTH_TOKEN_REFRESH_SUCCESS, jwtTokenResponse)
                .getBody());
  }

  @Operation(summary = "로그아웃", description = "Access Token과 Refresh Token 쿠키를 삭제합니다.")
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse> logout(
      @CookieValue(value = "accessToken", required = false) String accessToken) {

    if (accessToken == null) {
      throw new GeneralException(ErrorStatus.AUTH_INVALID_TOKEN_FORMAT);
    }

    authFacade.logout(accessToken);

    // Access Token 쿠키 삭제
    ResponseCookie accessCookie =
        ResponseCookie.from("accessToken", "")
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .sameSite(cookieProperties.getSameSite())
            .maxAge(0)
            .build();

    // Refresh Token 쿠키 삭제
    ResponseCookie refreshCookie =
        ResponseCookie.from("refreshToken", "")
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .sameSite(cookieProperties.getSameSite())
            .maxAge(0)
            .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
        .body(ApiResponse.onSuccess(SuccessStatus.AUTH_LOGOUT_SUCCESS).getBody());
  }

  @Operation(summary = "인증 확인", description = "현재 로그인 상태를 확인합니다.")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse> checkAuth() {
    // SecurityContext에서 인증 정보 가져오기
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 비로그인 상태 - 200 OK로 응답
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {

      AuthStatusResponse response = AuthStatusResponse.builder()
          .isAuthenticated(false)
          .memberId(null)
          .build();

      return ApiResponse.onSuccess(SuccessStatus.OK, response);
    }

    // 로그인 상태 - 200 OK로 응답
    AuthStatusResponse response = AuthStatusResponse.builder()
        .isAuthenticated(true)
        .memberId(authentication.getName())
        .build();

    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
