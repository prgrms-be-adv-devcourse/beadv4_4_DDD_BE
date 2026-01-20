package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressAddUseCase {

  private final MemberSupport memberSupport;

  public void execute(Long memberId, MemberDeliveryAddressCreateRequest request) {
    Member member = memberSupport.getMember(memberId);

    MemberDeliveryAddress address =
        MemberDeliveryAddress.builder()
            .member(member)
            .recipientName(request.recipientName())
            .recipientPhone(request.recipientPhone())
            .zipCode(request.zipCode())
            .address(request.address())
            .addressDetail(request.addressDetail())
            .addressName(request.addressName())
            .isDefault(request.isDefault() != null && request.isDefault())
            .build();

    if (Boolean.TRUE.equals(request.isDefault())) {
      member.setNewDefaultAddress(address);
    } else {
      member.addAddress(address);
    }
  }
}
