package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressDeleteUseCase {
  private final MemberSupport memberSupport;

  public void execute(Long memberId, Long addressId) {
    Member member = memberSupport.getMember(memberId);
    member.deleteDeliveryAddress(addressId);
  }
}
