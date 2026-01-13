package com.modeunsa.boundedcontext.payment.app.usecase;

import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberRepository;
import com.modeunsa.global.eventpublisher.SpringDomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Service
@RequiredArgsConstructor
public class PaymentSyncMemberUseCase {

  private final PaymentMemberRepository paymentMemberRepository;
  private final SpringDomainEventPublisher eventPublisher;

  @Transactional
  public void createPaymentMember(PaymentMemberDto paymentMemberDto) {

    PaymentMember paymentMember =
        PaymentMember.create(
            paymentMemberDto.getMemberId(),
            paymentMemberDto.getEmail(),
            paymentMemberDto.getName(),
            paymentMemberDto.getStatus());

    PaymentMember savedMember = paymentMemberRepository.save(paymentMember);

    eventPublisher.publish(new PaymentMemberCreatedEvent(savedMember.getId()));
  }
}
