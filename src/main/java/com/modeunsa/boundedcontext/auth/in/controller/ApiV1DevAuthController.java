package com.modeunsa.boundedcontext.auth.in.controller;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.global.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(
      summary = "개발용 프리패스 로그인",
      description =
          """
          OAuth 인증 과정을 생략하고 Member ID만으로 즉시 Access/Refresh Token을 발급받습니다.<br>
          MemberDataInit으로 생성된 더미 데이터의 ID를 확인 후 사용하세요.<br>
          """)
  @PostMapping("/login")
  public DevLoginResponse devLogin(@RequestParam Long memberId) {
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 기존 로그인 로직에서 사용하는 토큰 생성 메서드를 호출
    String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRole());

    return new DevLoginResponse(accessToken, refreshToken);
  }

  public record DevLoginResponse(String accessToken, String refreshToken) {}
}
