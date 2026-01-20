package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.dto.request.MemberDeliveryAddressCreateRequest;
import com.modeunsa.shared.member.event.MemberDeliveryAddressAddedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressAddUseCase {

  private final MemberSupport memberSupport;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public void execute(Long memberId, MemberDeliveryAddressCreateRequest request) {
    Member member = memberSupport.getMember(memberId);

    boolean isDefaultRequest = Boolean.TRUE.equals(request.isDefault());

    // 기본 배송지로 설정하려는 경우, 이미 기본 배송지가 있는지 확인
    if (isDefaultRequest && member.getDefaultDeliveryAddress() != null) {
      throw new GeneralException(ErrorStatus.MEMBER_ALREADY_HAS_DEFAULT_ADDRESS);
    }

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

    eventPublisher.publishEvent(
        MemberDeliveryAddressAddedEvent.of(
            memberId, address.getId(), address.getAddressName(), isDefaultRequest));
  }
}
