package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.boundedcontext.member.domain.dto.request.MemberDeliveryAddressCreateRequest;
import com.modeunsa.shared.member.event.MemberDeliveryAddressAddedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressAddUseCase {

  private final MemberSupport memberSupport;
  private final EventPublisher eventPublisher;

  @Transactional
  public void execute(Long memberId, MemberDeliveryAddressCreateRequest request) {
    Member member = memberSupport.getMember(memberId);

    boolean isDefaultRequest = Boolean.TRUE.equals(request.isDefault());

    member.validateCanRegisterDefaultAddress(isDefaultRequest);

    MemberDeliveryAddress address =
        MemberDeliveryAddress.builder()
            .recipientName(request.recipientName())
            .recipientPhone(request.recipientPhone())
            .zipCode(request.zipCode())
            .address(request.address())
            .addressDetail(request.addressDetail())
            .addressName(request.addressName())
            .isDefault(isDefaultRequest)
            .build();

    member.addAddress(address);

    eventPublisher.publish(
        new MemberDeliveryAddressAddedEvent(
            memberId,
            address.getId(),
            address.getRecipientName(),
            address.getRecipientPhone(),
            address.getZipCode(),
            address.getAddress(),
            address.getAddressDetail(),
            address.getAddressName(),
            address.getIsDefault()));
  }
}
