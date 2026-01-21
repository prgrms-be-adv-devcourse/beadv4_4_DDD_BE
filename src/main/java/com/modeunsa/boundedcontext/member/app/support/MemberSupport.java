package com.modeunsa.boundedcontext.member.app.support;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberSupport {
  private final MemberRepository memberRepository;

  public Member getMember(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
  }

  public MemberProfile getMemberProfileOrThrow(Long memberId) {
    Member member = getMember(memberId);
    MemberProfile profile = member.getProfile();

    if (profile == null) {
      throw new GeneralException(ErrorStatus.MEMBER_PROFILE_NOT_FOUND);
    }
    return profile;
  }
}
