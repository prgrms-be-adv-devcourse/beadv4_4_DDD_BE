package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
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

    PaymentMember paymentMember =
        PaymentMember.create(member.id(), member.email(), member.name(), member.status());

    PaymentMember savedMember = paymentMemberRepository.save(paymentMember);

    eventPublisher.publish(new PaymentMemberCreatedEvent(savedMember.getId()));
  }
}
