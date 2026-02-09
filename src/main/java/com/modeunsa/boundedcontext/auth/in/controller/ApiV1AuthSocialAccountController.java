package com.modeunsa.boundedcontext.auth.in.controller;

import com.modeunsa.boundedcontext.auth.app.facade.OAuthAccountFacade;
import com.modeunsa.boundedcontext.auth.domain.types.OAuthProvider;
import com.modeunsa.boundedcontext.auth.in.util.AuthRequestUtils;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.auth.dto.SocialStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth Social Account", description = "소셜 계정 연동 API")
@RestController
@RequestMapping("/api/v1/auths/social-accounts")
@RequiredArgsConstructor
public class ApiV1AuthSocialAccountController {

  private final OAuthAccountFacade oauthAccountFacade;

  @Operation(summary = "소셜 계정 연동 URL 조회", description = "마이페이지에서 소셜 계정 연동을 위한 OAuth URL을 반환합니다.")
  @GetMapping("/{provider}/link-url")
  public ResponseEntity<ApiResponse> getLinkUrl(
      @AuthenticationPrincipal CustomUserDetails user,
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "리다이렉트 URI") @RequestParam(required = false) String redirectUri) {

    Long memberId = user.getMemberId();
    OAuthProvider oauthProvider = AuthRequestUtils.findProvider(provider);
    String linkUrl = oauthAccountFacade.getLinkUrl(memberId, oauthProvider, redirectUri);

    return ApiResponse.onSuccess(SuccessStatus.OK, linkUrl);
  }

  @Operation(summary = "소셜 계정 연동", description = "OAuth 인증 완료 후 소셜 계정을 현재 회원에 연동합니다.")
  @PostMapping("/{provider}/link")
  public ResponseEntity<ApiResponse> linkSocialAccount(
      @AuthenticationPrincipal CustomUserDetails user,
      @Parameter(description = "OAuth 제공자", example = "kakao") @PathVariable String provider,
      @Parameter(description = "인증 코드", required = true) @RequestParam String code,
      @Parameter(description = "리다이렉트 URI") @RequestParam(required = false) String redirectUri,
      @Parameter(description = "state 값", required = true) @RequestParam String state) {

    Long memberId = user.getMemberId();
    OAuthProvider oauthProvider = AuthRequestUtils.findProvider(provider);
    oauthAccountFacade.linkSocialAccount(memberId, oauthProvider, code, redirectUri, state);

    return ApiResponse.onSuccess(SuccessStatus.SOCIAL_ACCOUNT_LINK_SUCCESS);
  }

  @Operation(summary = "소셜 계정 연동 상태 조회", description = "현재 로그인한 사용자의 플랫폼별 연동 여부를 반환합니다.")
  @GetMapping("/status")
  public ResponseEntity<ApiResponse> getSocialStatus(
      @AuthenticationPrincipal CustomUserDetails user) {

    Long memberId = user.getMemberId();
    SocialStatusResponse status = oauthAccountFacade.getSocialStatus(memberId);

    return ApiResponse.onSuccess(SuccessStatus.OK, status);
  }
}
