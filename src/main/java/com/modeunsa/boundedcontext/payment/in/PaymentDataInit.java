package com.modeunsa.boundedcontext.payment.in;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class PaymentDataInit {

  private final PaymentDataInit self;
  private final PaymentFacade paymentFacade;

  private static final String TRACE_ID_MDC_KEY = "TRACE_ID";

  public PaymentDataInit(@Lazy PaymentDataInit self, PaymentFacade paymentFacade) {
    this.self = self;
    this.paymentFacade = paymentFacade;
  }

  @Bean
  public ApplicationRunner paymentDataInitApplicationRunner() {
    return args -> {
      String traceId = UUID.randomUUID().toString();
      MDC.put(TRACE_ID_MDC_KEY, traceId);

      try {
        self.makeBasePaymentMembers();
        self.makeBaseCredits();
      } finally {
        MDC.clear();
      }
    };
  }

  @Transactional
  public void makeBasePaymentMembers() {

    PaymentMemberDto systemMember =
        new PaymentMemberDto(1L, "system@example.com", "시스템", MemberStatus.ACTIVE);

    PaymentMemberDto holderMember =
        new PaymentMemberDto(2L, "holder@example.com", "홀더", MemberStatus.ACTIVE);

    PaymentMemberDto paymentMember1 =
        new PaymentMemberDto(3L, "user1@example.com", "사용자1", MemberStatus.ACTIVE);

    PaymentMemberDto paymentMember2 =
        new PaymentMemberDto(4L, "user2@example.com", "사용자2", MemberStatus.ACTIVE);

    PaymentMemberDto paymentMember3 =
        new PaymentMemberDto(5L, "user3@example.com", "사용자3", MemberStatus.ACTIVE);

    paymentFacade.createPaymentMember(systemMember);
    paymentFacade.createPaymentMember(holderMember);
    paymentFacade.createPaymentMember(paymentMember1);
    paymentFacade.createPaymentMember(paymentMember2);
    paymentFacade.createPaymentMember(paymentMember3);
  }

  @Transactional
  public void makeBaseCredits() {
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            3L, BigDecimal.valueOf(150_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            4L, BigDecimal.valueOf(20_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            5L, BigDecimal.valueOf(50_000), PaymentEventType.CHARGE_BANK_TRANSFER));
  }
}
