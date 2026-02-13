package com.modeunsa.boundedcontext.member.app.support;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.domain.entity.MemberSeller;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberSellerRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSupport {
  private final MemberRepository memberRepository;
  private final MemberSellerRepository memberSellerRepository;

  // TODO: 성능 개선 - DTO로 변환 후 캐싱 처리 고려
  public Member getMember(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
  }

  public MemberProfile getMemberProfileOrThrow(Long memberId) {
    // 1. Member 조회
    Member member =
        memberRepository
            .findByIdWithProfile(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

    // 2. Member는 있는데 Profile이 null인 경우 체크
    MemberProfile profile = member.getProfile();
    if (profile == null) {
      throw new GeneralException(ErrorStatus.MEMBER_PROFILE_NOT_FOUND);
    }

    return profile;
  }

  public Long getSellerIdByMemberId(Long memberId) {
    return memberSellerRepository.findByMemberId(memberId).map(MemberSeller::getId).orElse(null);
  }
}
