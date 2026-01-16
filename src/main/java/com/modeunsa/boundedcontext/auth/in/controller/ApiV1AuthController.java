package com.modeunsa.boundedcontext.auth.in.controller;

import com.modeunsa.boundedcontext.auth.app.facade.AuthFacade;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.auth.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

  private final AuthFacade authFacade;

  @Operation(summary = "OAuth2 로그인 URL 조회", description = "OAuth2 로그인 URL을 반환합니다.")
  @GetMapping("/oauth/{provider}/url")
  public ResponseEntity<ApiResponse> getOAuthLoginUrl(
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "리다이렉트 URI") @RequestParam(required = false) String redirectUri) {
    OAuthProvider oauthProvider =
        Arrays.stream(OAuthProvider.values())
            .filter(p -> p.name().equalsIgnoreCase(provider))
            .findFirst()
            .orElseThrow(() -> new GeneralException(ErrorStatus.OAUTH_INVALID_PROVIDER));
    String loginUrl = authFacade.getOAuthLoginUrl(oauthProvider, redirectUri);
    return ApiResponse.onSuccess(SuccessStatus.OK, loginUrl);
  }

  @Operation(summary = "소셜 로그인", description = "소셜 로그인 인증 코드를 사용하여 로그인 및 토큰을 발급합니다.")
  @PostMapping("/login/{provider}")
  public ResponseEntity<ApiResponse> login(
      @Parameter(description = "OAuth 제공자", example = "kakao")
      @PathVariable String provider,
      @Parameter(description = "인증 코드", required = true)
      @RequestParam String code
  ) {
    OAuthProvider oauthProvider = findProvider(provider);

    /**
    /* TODO: 실제 구현 시에는 provider와 code를 Facade에 넘겨서
    /* 1. 소셜 회원 정보 조회 -> 2. 회원가입/로그인 처리 -> 3. MemberId 추출 과정을 거쳐야 합니다.
    /* 현재는 구조 설명을 위해 임의의 ID(1L)와 Role(USER)을 넘깁니다.
     */
    TokenResponse tokenResponse = authFacade.login(1L, MemberRole.MEMBER);

    return ApiResponse.onSuccess(SuccessStatus.AUTH_LOGIN_SUCCESS, tokenResponse);
  }

  @Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 Access Token을 재발급합니다.")
  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse> reissue(
      @Parameter(description = "Refresh Token", required = true)
      @RequestHeader("RefreshToken") String refreshToken
  ) {
    // TODO: 실제로는 Refresh Token 내부의 정보나 DB 조회를 통해 Role을 가져와야 할 수 있습니다.
    TokenResponse tokenResponse = authFacade.reissueToken(refreshToken);

    return ApiResponse.onSuccess(SuccessStatus.AUTH_TOKEN_REFRESH_SUCCESS, tokenResponse);
  }

  // Provider 문자열 -> Enum 변환 (중복 제거를 위해 추출)
  private OAuthProvider findProvider(String providerName) {
    return Arrays.stream(OAuthProvider.values())
        .filter(p -> p.name().equalsIgnoreCase(providerName))
        .findFirst()
        .orElseThrow(() -> new GeneralException(ErrorStatus.OAUTH_INVALID_PROVIDER));
  }
}
