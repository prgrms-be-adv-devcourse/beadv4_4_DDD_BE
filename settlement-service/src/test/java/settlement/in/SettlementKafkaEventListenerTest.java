package settlement.in;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.modeunsa.boundedcontext.settlement.app.SettlementFacade;
import com.modeunsa.boundedcontext.settlement.in.SettlementKafkaEventListener;
import com.modeunsa.global.eventpublisher.topic.DomainEventEnvelope;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.shared.member.event.MemberSignupEvent;
import com.modeunsa.shared.order.dto.OrderDto;
import com.modeunsa.shared.order.event.OrderPurchaseConfirmedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementKafkaEventListener 테스트")
class SettlementKafkaEventListenerTest {

  @Mock private SettlementFacade settlementFacade;

  @Mock private JsonConverter jsonConverter;

  @InjectMocks private SettlementKafkaEventListener listener;

  @Test
  @DisplayName("MemberSignupEvent 수신 시 settlementFacade.syncMember 호출")
  void handleMemberEventMemberSignupEvent() {
    // given
    Long memberId = 1L;
    String role = "SELLER";
    String payload = "{\"memberId\":1,\"role\":\"SELLER\"}";

    DomainEventEnvelope envelope = mock(DomainEventEnvelope.class);
    when(envelope.eventType()).thenReturn("MemberSignupEvent");
    when(envelope.payload()).thenReturn(payload);

    MemberSignupEvent event =
        new MemberSignupEvent(memberId, "홍길동", "test@test.com", "010-0000-0000", role, "ACTIVE");
    Acknowledgment ack = mock(Acknowledgment.class);

    when(jsonConverter.deserialize(payload, MemberSignupEvent.class)).thenReturn(event);

    // when
    try (MockedStatic<TransactionSynchronizationManager> tx =
        org.mockito.Mockito.mockStatic(TransactionSynchronizationManager.class)) {
      tx.when(
              () ->
                  TransactionSynchronizationManager.registerSynchronization(
                      org.mockito.ArgumentMatchers.any(TransactionSynchronization.class)))
          .thenAnswer(
              invocation -> {
                TransactionSynchronization sync = invocation.getArgument(0);
                sync.afterCommit();
                return null;
              });

      listener.handleMemberEvent(envelope, ack);
    }

    // then
    verify(settlementFacade).syncMember(memberId, role);
    verify(ack).acknowledge();
  }

  @Test
  @DisplayName("OrderPurchaseConfirmedEvent 수신 시 settlementFacade.collectCandidateItems 호출")
  void handleOrderEventOrderPurchaseConfirmedEvent() {
    // given
    Long orderId = 100L;
    String payload = "{\"orderId\":100}";

    DomainEventEnvelope envelope = mock(DomainEventEnvelope.class);
    when(envelope.eventType()).thenReturn("OrderPurchaseConfirmedEvent");
    when(envelope.payload()).thenReturn(payload);

    OrderDto orderDto = OrderDto.builder().orderId(orderId).build();
    OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(orderDto, "trace-id");
    Acknowledgment ack = mock(Acknowledgment.class);

    when(jsonConverter.deserialize(payload, OrderPurchaseConfirmedEvent.class)).thenReturn(event);

    // when
    try (MockedStatic<TransactionSynchronizationManager> tx =
        org.mockito.Mockito.mockStatic(TransactionSynchronizationManager.class)) {
      tx.when(
              () ->
                  TransactionSynchronizationManager.registerSynchronization(
                      org.mockito.ArgumentMatchers.any(TransactionSynchronization.class)))
          .thenAnswer(
              invocation -> {
                TransactionSynchronization sync = invocation.getArgument(0);
                sync.afterCommit();
                return null;
              });

      listener.handleOrderEvent(envelope, ack);
    }

    // then
    verify(settlementFacade).collectCandidateItems(orderId);
    verify(ack).acknowledge();
  }
}
