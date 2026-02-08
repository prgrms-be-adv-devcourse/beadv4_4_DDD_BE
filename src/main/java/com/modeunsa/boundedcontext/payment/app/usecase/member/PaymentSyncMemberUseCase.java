package com.modeunsa.boundedcontext.payment.app.usecase.member;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberStore;
import com.modeunsa.global.eventpublisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentSyncMemberUseCase {

  private final PaymentMemberStore paymentMemberStore;
  private final EventPublisher eventPublisher;

  public void createPaymentMember(PaymentMemberDto member) {

    PaymentMember paymentMember =
        PaymentMember.create(member.id(), member.email(), member.name(), member.status());

    PaymentMember savedMember = paymentMemberStore.store(paymentMember);

    eventPublisher.publish(new PaymentMemberCreatedEvent(savedMember.getId()));
  }
}
