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
            .recipientName(request.getRecipientName())
            .recipientPhone(request.getRecipientPhone())
            .zipCode(request.getZipCode())
            .address(request.getAddress())
            .addressDetail(request.getAddressDetail())
            .addressName(request.getAddressName())
            .isDefault(request.getIsDefault() != null && request.getIsDefault())
            .build();

    if (Boolean.TRUE.equals(request.getIsDefault())) {
      member.setNewDefaultAddress(address);
    } else {
      member.addAddress(address);
    }
  }
}
