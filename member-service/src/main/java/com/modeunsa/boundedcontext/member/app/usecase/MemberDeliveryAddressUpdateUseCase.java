package com.modeunsa.boundedcontext.member.app.usecase;

import com.modeunsa.boundedcontext.member.app.support.MemberSupport;
import com.modeunsa.boundedcontext.member.domain.entity.Member;
import com.modeunsa.boundedcontext.member.domain.entity.MemberDeliveryAddress;
import com.modeunsa.boundedcontext.member.out.repository.MemberDeliveryAddressRepository;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.boundedcontext.member.domain.dto.request.MemberDeliveryAddressUpdateRequest;
import com.modeunsa.shared.member.event.MemberDeliveryAddressUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDeliveryAddressUpdateUseCase {

  private final MemberSupport memberSupport;
  private final MemberDeliveryAddressRepository addressRepository;
  private final EventPublisher eventPublisher;

  public void execute(Long memberId, Long addressId, MemberDeliveryAddressUpdateRequest request) {
    Member member = memberSupport.getMember(memberId);

    // 1. 배송지 조회
    MemberDeliveryAddress address =
        addressRepository
            .findById(addressId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ADDRESS_NOT_FOUND));

    // 2. 권한 검증 (내 배송지가 맞는지)
    if (!address.getMember().getId().equals(member.getId())) {
      throw new GeneralException(ErrorStatus.ADDRESS_ACCESS_DENIED);
    }

    // 3. 정보 수정
    address
        .updateRecipientName(request.getRecipientName())
        .updateRecipientPhone(request.getRecipientPhone())
        .updateZipCode(request.getZipCode())
        .updateAddress(request.getAddress())
        .updateAddressDetail(request.getAddressDetail())
        .updateAddressName(request.getAddressName());

    // 4. 이벤트 발행
    eventPublisher.publish(
        new MemberDeliveryAddressUpdatedEvent(
            memberId,
            addressId,
            address.getRecipientName(),
            address.getRecipientPhone(),
            address.getZipCode(),
            address.getAddress(),
            address.getAddressDetail(),
            address.getAddressName(),
            address.getIsDefault()));
  }
}
