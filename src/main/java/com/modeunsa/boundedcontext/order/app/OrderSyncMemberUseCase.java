package com.modeunsa.boundedcontext.order.app;

import com.modeunsa.boundedcontext.order.domain.OrderMapper;
import com.modeunsa.boundedcontext.order.domain.OrderMember;
import com.modeunsa.boundedcontext.order.out.OrderMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderSyncMemberUseCase {
  private final OrderMemberRepository orderMemberRepository;
  private final OrderMapper orderMapper;

  public void syncMember(Long memberId, String memberName, String memberPhone) {
    OrderMember member = orderMapper.toOrderMember(memberId, memberName, memberPhone);

    orderMemberRepository.save(member);
  }
}
