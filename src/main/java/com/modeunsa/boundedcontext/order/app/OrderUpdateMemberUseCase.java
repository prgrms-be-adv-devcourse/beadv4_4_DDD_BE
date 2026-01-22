package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderUpdateMemberUseCase {
  private final OrderMemberRepository orderMemberRepository;

  public void updateMember(Long memberId, String memberName, String memberPhone) {
    OrderMember member =
        orderMemberRepository
            .findById(memberId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.ORDER_MEMBER_NOT_FOUND));

    member.updateInfo(memberName, memberPhone);
  }
}
