package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateDeliveryAddressUseCase {
  private final OrderSupport orderSupport;

  public void createDeliveryAddress(
      Long memberId,
      String recipientName,
      String recipientPhone,
      String zipCode,
      String address,
      String addressDetail,
      String addressName) {
    OrderMember member = orderSupport.findByMemberId(memberId);

    member.createDeliveryAddress(
        recipientName, recipientPhone, zipCode, address, addressDetail, addressName);
  }
}
