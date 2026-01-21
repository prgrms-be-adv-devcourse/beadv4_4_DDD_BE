package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentSyncMemberUseCase {

  private final PaymentMemberRepository paymentMemberRepository;
  private final SpringDomainEventPublisher eventPublisher;

  public void createPaymentMember(PaymentMemberDto member) {

    // TODO: MemberSignupEvent 에서 name, status 값을 추가로 받은 이후에 반영할 것
    PaymentMember paymentMember =
        PaymentMember.create(member.id(), member.email(), "사용자", MemberStatus.ACTIVE);

    PaymentMember savedMember = paymentMemberRepository.save(paymentMember);

    eventPublisher.publish(new PaymentMemberCreatedEvent(savedMember.getId()));
  }
}
