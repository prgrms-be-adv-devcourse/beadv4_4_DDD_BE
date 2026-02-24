package com.modeunsa.boundedcontext.payment.app.usecase.member;

import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberSyncRequest;
import com.modeunsa.boundedcontext.payment.domain.entity.PaymentMember;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberReader;
import com.modeunsa.boundedcontext.payment.out.PaymentMemberStore;
import com.modeunsa.global.eventpublisher.EventPublisher;
import com.modeunsa.shared.payment.event.PaymentMemberCreatedEvent;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentSyncMemberUseCase {

  private final PaymentMemberReader paymentMemberReader;
  private final PaymentMemberStore paymentMemberStore;
  private final EventPublisher eventPublisher;

  public void execute(PaymentMemberSyncRequest member) {

    Optional<PaymentMember> find = paymentMemberReader.findById(member.id());
    if (find.isPresent()) {
      PaymentMember existing = find.get();
      existing.sync(member.email(), member.name(), member.status());
      return;
    }

    PaymentMember paymentMember =
        PaymentMember.create(member.id(), member.email(), member.name(), member.status());

    PaymentMember savedMember = paymentMemberStore.store(paymentMember);
    eventPublisher.publish(new PaymentMemberCreatedEvent(savedMember.getId()));
  }
}
