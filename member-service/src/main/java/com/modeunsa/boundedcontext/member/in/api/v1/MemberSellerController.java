package com.modeunsa.boundedcontext.member.in.api.v1;

import com.modeunsa.boundedcontext.auth.domain.dto.JwtTokenResponse;
import com.modeunsa.boundedcontext.member.app.facade.MemberSellerFacade;
import com.modeunsa.boundedcontext.member.domain.dto.request.SellerRegisterRequest;
import com.modeunsa.global.config.CookieProperties;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Member", description = "회원 API")
@RestController("MemberSellerV1Controller")
@RequestMapping("/api/v1/members/me")
@RequiredArgsConstructor
public class MemberSellerController {
  private final MemberSellerFacade memberSellerFacade;
  private final CookieProperties cookieProperties;

  /** 판매자 등록 요청 */
  @Operation(summary = "판매자 등록 요청", description = "판매자 등록을 요청합니다.")
  @PostMapping("/seller/register")
  public ResponseEntity<ApiResponse> registerSeller(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user,
      @RequestBody @Valid SellerRegisterRequest request) {

    Long memberId = user.getMemberId();

    // 1. 판매자 등록 후 새 토큰 발급 받기
    JwtTokenResponse jwtTokenResponse = memberSellerFacade.registerSeller(memberId, request);

    // 2. Access Token 쿠키 생성
    ResponseCookie accessTokenCookie =
        ResponseCookie.from("accessToken", jwtTokenResponse.accessToken())
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(jwtTokenResponse.accessTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    // 3. Refresh Token 쿠키 생성
    ResponseCookie refreshTokenCookie =
        ResponseCookie.from("refreshToken", jwtTokenResponse.refreshToken())
            .httpOnly(true)
            .secure(cookieProperties.isSecure())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofMillis(jwtTokenResponse.refreshTokenExpiresIn()))
            .sameSite(cookieProperties.getSameSite())
            .build();

    // 4. 쿠키 헤더 설정 및 응답 반환
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
        .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
        .body(ApiResponse.onSuccess(
            SuccessStatus.SELLER_REGISTER_SUCCESS, jwtTokenResponse).getBody());
  }
}
