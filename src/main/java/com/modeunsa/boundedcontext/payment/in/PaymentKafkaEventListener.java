package com.modeunsa.boundedcontext.payment.in;

import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.event.PaymentMemberCreatedEvent;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class PaymentKafkaEventListener {

  private final PaymentFacade paymentFacade;
  private final PaymentMapper paymentMapper;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "member-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handleMemberEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case "MemberSignupEvent" -> {
        MemberSignupEvent event =
            objectMapper.readValue(envelope.payload(), MemberSignupEvent.class);
        PaymentMemberDto member = paymentMapper.toPaymentMemberDto(event);
        paymentFacade.createPaymentMember(member);
      }
      default -> {
        // ignore}
      }
    }
  }

  @KafkaListener(topics = "payment-events", groupId = "payment-service")
  @Transactional(propagation = REQUIRES_NEW)
  public void handlePaymentEvent(DomainEventEnvelope envelope) {
    switch (envelope.eventType()) {
      case "PaymentMemberCreatedEvent" -> {
        PaymentMemberCreatedEvent event =
            objectMapper.readValue(envelope.payload(), PaymentMemberCreatedEvent.class);
        paymentFacade.createPaymentAccount(event.memberId());
      }
      default -> {
        // ignore
      }
    }
  }
}
