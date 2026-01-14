package com.modeunsa.boundedcontext.payment.in;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.domain.types.MemberStatus;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import java.math.BigDecimal;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

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
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            1L, new BigDecimal(150_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            2L, new BigDecimal(100_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            3L, new BigDecimal(50_000), PaymentEventType.CHARGE_BANK_TRANSFER));
  }
}
