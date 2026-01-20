package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.shared.member.dto.request.MemberBasicInfoUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberBasicInfoUpdateUseCase {
  private final MemberSupport memberReader;

  public void execute(Long memberId, MemberBasicInfoUpdateRequest request) {
    Member member = memberReader.getMember(memberId);

    member
        .updateRealName(request.getRealName())
        .updatePhoneNumber(request.getPhoneNumber())
        .updateEmail(request.getEmail());
  }
}
