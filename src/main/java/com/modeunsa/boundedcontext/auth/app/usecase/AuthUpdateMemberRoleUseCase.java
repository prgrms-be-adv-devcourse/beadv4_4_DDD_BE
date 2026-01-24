package com.modeunsa.boundedcontext.auth.app.usecase;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.security.jwt.JwtTokenProvider;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.dto.response.MemberRoleUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthUpdateMemberRoleUseCase {
  private final MemberRepository memberRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Transactional
  public MemberRoleUpdateResponse execute(Long memberId, MemberRole newRole) {
    // 1. 회원 조회
    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 2. 권한 변경
    member.changeRole(newRole);

    // 3. 변경 사항 DB 반영 (Dirty Checking으로 인해 사실 save 생략 가능하지만 명시적 유지)
    memberRepository.save(member);

    // 4. 변경된 Role을 바탕으로 새 토큰 세트 생성
    String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRole());
    String refreshToken = jwtTokenProvider.createRefreshToken(member.getId(), member.getRole());

    return new MemberRoleUpdateResponse(accessToken, refreshToken);
  }
}
