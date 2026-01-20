package com.modeunsa.boundedcontext.member.app.support;

import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberProfile;
import com.modeunsa.boundedcontext.member.out.repository.MemberProfileRepository;
import com.modeunsa.boundedcontext.member.out.repository.MemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberSupport {
  private final MemberRepository memberRepository;
  private final MemberProfileRepository memberProfileRepository;

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
