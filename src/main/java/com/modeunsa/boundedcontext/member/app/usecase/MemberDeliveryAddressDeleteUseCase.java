package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressDeleteUseCase {
  private final MemberSupport memberSupport;

  public void execute(Long memberId, Long addressId) {
    Member member = memberSupport.getMember(memberId);

    MemberDeliveryAddress address =
        member.getAddresses().stream()
            .filter(a -> a.getId().equals(addressId))
            .findFirst()
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_DELIVERY_ADDRESS_NOT_FOUND));

    member.deleteDeliveryAddress(address);
  }
}
