package com.modeunsa.boundedcontext.payment.in;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
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
        self.makeBaseCredits();
      } finally {
        MDC.clear();
      }
    };
  }

  @Transactional
  public void makeBaseCredits() {

    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            1L, BigDecimal.valueOf(100_000), PaymentEventType.CHARGE_BANK_TRANSFER));

    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            2L, BigDecimal.valueOf(100_000), PaymentEventType.CHARGE_BANK_TRANSFER));

    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            3L, BigDecimal.valueOf(100_000), PaymentEventType.CHARGE_BANK_TRANSFER));

    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            4L, BigDecimal.valueOf(20_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            5L, BigDecimal.valueOf(150_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        new PaymentAccountDepositRequest(
            6L, BigDecimal.valueOf(50_000), PaymentEventType.CHARGE_BANK_TRANSFER));
  }
}
