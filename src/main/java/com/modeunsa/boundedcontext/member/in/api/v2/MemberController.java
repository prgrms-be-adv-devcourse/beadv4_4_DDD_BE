package com.modeunsa.boundedcontext.member.in.api.v2;

import com.modeunsa.boundedcontext.member.app.facade.MemberFacade;
import com.modeunsa.global.config.CookieProperties;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import com.modeunsa.shared.member.dto.request.MemberSignupCompleteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController("MemberV2Controller")
@RequestMapping("/api/v2/members")
@RequiredArgsConstructor
public class MemberController {
  private final MemberFacade memberFacade;
  private final CookieProperties cookieProperties;

  @Operation(summary = "회원가입 완료 처리", description = "소셜 로그인 직후(PRE_ACTIVE) 추가 정보를 입력받아 정회원(ACTIVE)으로 전환합니다.")
  @PostMapping("/signup-complete")
  public ResponseEntity<ApiResponse> completeSignup(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails userDetails,
      @RequestBody @Valid MemberSignupCompleteRequest request) {

    Long memberId = Long.parseLong(userDetails.getUsername());

    // 1. 로직 실행 및 새 토큰 발급 받기
    JwtTokenResponse jwtTokenResponse = memberFacade.completeSignup(memberId, request);

    // 2. Access Token 쿠키 생성 (ACTIVE 상태)
    ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", jwtTokenResponse.accessToken())
        .httpOnly(cookieProperties.isHttpOnly())
        .secure(cookieProperties.isSecure())
        .path(cookieProperties.getPath())
        .maxAge(Duration.ofMillis(jwtTokenResponse.accessTokenExpiresIn()))
        .sameSite(cookieProperties.getSameSite())
        .build();

    // 3. Refresh Token 쿠키 생성
    ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", jwtTokenResponse.refreshToken())
        .httpOnly(true)
        .secure(cookieProperties.isSecure())
        .path(cookieProperties.getPath())
        .maxAge(Duration.ofMillis(jwtTokenResponse.refreshTokenExpiresIn()))
        .sameSite(cookieProperties.getSameSite())
        .build();

    // 4. 응답 헤더에 쿠키 설정하여 반환
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .body(ApiResponse.onSuccess(SuccessStatus.OK, jwtTokenResponse).getBody());
  }
}
