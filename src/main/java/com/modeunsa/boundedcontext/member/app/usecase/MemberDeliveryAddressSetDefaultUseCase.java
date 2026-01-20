package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.boundedcontext.member.out.repository.MemberDeliveryAddressRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressSetDefaultUseCase {
  private final MemberSupport memberSupport;
  private final MemberDeliveryAddressRepository addressRepository;

  public void execute(Long memberId, Long addressId) {
    Member member = memberSupport.getMember(memberId);

    MemberDeliveryAddress newDefaultAddress =
        addressRepository
            .findById(addressId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ADDRESS_NOT_FOUND));

    if (!newDefaultAddress.getMember().getId().equals(member.getId())) {
      throw new GeneralException(ErrorStatus.ADDRESS_ACCESS_DENIED);
    }

    member.setNewDefaultAddress(newDefaultAddress);
  }
}
