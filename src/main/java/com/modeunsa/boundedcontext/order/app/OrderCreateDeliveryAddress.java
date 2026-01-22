package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCreateDeliveryAddress {
  private final OrderSupport orderSupport;

  public void createDeliveryAddress(
      Long memberId, String zipCode, String address, String addressDetail) {
    OrderMember member = orderSupport.findByMemberId(memberId);

    member.createDeliveryAddress(zipCode, address, addressDetail);
  }
}
