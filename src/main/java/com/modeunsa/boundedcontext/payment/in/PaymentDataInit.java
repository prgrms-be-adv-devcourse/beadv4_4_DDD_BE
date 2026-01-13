package com.modeunsa.boundedcontext.payment.in;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author : JAKE
 * @date : 26. 1. 13.
 */
@Configuration
public class PaymentDataInit {

  private final PaymentDataInit self;
  private final PaymentFacade paymentFacade;

  public PaymentDataInit(@Lazy PaymentDataInit self, PaymentFacade paymentFacade) {
    this.self = self;
    this.paymentFacade = paymentFacade;
  }

  @Bean
  public ApplicationRunner paymentDataInitApplicationRunner() {
    return args -> {
      self.makeBasePaymentMembers();
      self.makeBaseCredits();
    };
  }

  @Transactional
  public void makeBasePaymentMembers() {

    PaymentMemberDto paymentMember1 =
        new PaymentMemberDto(1L, "user1@naver.com", "사용자1", MemberStatus.ACTIVE);

    PaymentMemberDto paymentMember2 =
        new PaymentMemberDto(2L, "user2@naver.com", "사용자2", MemberStatus.ACTIVE);

    PaymentMemberDto paymentMember3 =
        new PaymentMemberDto(3L, "user3@naver.com", "사용자3", MemberStatus.ACTIVE);

    paymentFacade.createPaymentMember(paymentMember1);
    paymentFacade.createPaymentMember(paymentMember2);
    paymentFacade.createPaymentMember(paymentMember3);
  }

  public void makeBaseCredits() {
    paymentFacade.creditAccount(1L, 150_000, PaymentEventType.CHARGE_BANK_TRANSFER);
    paymentFacade.creditAccount(2L, 100_000, PaymentEventType.CHARGE_BANK_TRANSFER);
    paymentFacade.creditAccount(3L, 50_000, PaymentEventType.CHARGE_BANK_TRANSFER);
  }
}
