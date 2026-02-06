package com.modeunsa.boundedcontext.auth.in.controller;

import com.modeunsa.boundedcontext.auth.app.facade.AuthFacade;
import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.auth.dto.JwtTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth (Dev)", description = "개발 환경 전용 인증/인가 테스트 API")
@Profile("!prod")
@RestController
@RequestMapping("/api/v1/auths/dev")
@RequiredArgsConstructor
public class ApiV1DevAuthController {
  private final MemberRepository memberRepository;
  private final MemberSupport memberSupport;
  private final AuthFacade authFacade;

  @Operation(
      summary = "개발용 프리패스 로그인",
      description =
          """
          OAuth 인증 과정을 생략하고 Member ID만으로 즉시 Access/Refresh Token을 발급받습니다.<br>
          MemberDataInit으로 생성된 더미 데이터의 ID를 확인 후 사용하세요.<br>
          """)
  @PostMapping("/login")
  public JwtTokenResponse devLogin(@RequestParam Long memberId, HttpServletResponse response) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    Long sellerId = memberSupport.getSellerIdByMemberId(memberId);
    JwtTokenResponse tokenResponse = authFacade.login(member.getId(), member.getRole(), sellerId);

    // Access Token 쿠키
    Cookie accessCookie = new Cookie("accessToken", tokenResponse.accessToken());
    accessCookie.setPath("/");
    accessCookie.setHttpOnly(true);
    accessCookie.setMaxAge((int) tokenResponse.accessTokenExpiresIn() / 1000);
    response.addCookie(accessCookie);

    // Refresh Token 쿠키
    Cookie refreshCookie = new Cookie("refreshToken", tokenResponse.refreshToken());
    refreshCookie.setPath("/");
    refreshCookie.setHttpOnly(true);
    refreshCookie.setMaxAge((int) tokenResponse.refreshTokenExpiresIn() / 1000);
    response.addCookie(refreshCookie);

    return tokenResponse;
  }
}
