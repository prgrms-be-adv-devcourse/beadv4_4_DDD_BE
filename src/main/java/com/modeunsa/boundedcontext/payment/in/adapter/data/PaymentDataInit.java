package com.modeunsa.boundedcontext.payment.in.adapter.data;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;

@Profile("!test")
@ConditionalOnProperty(name = "app.data-init.enabled", havingValue = "true", matchIfMissing = true)
// @Configuration
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

    if (paymentFacade.countAccountLog() > 0) {
      return;
    }

    paymentFacade.creditAccount(
        1L,
        new PaymentAccountDepositRequest(
            BigDecimal.valueOf(100_000), PaymentEventType.CHARGE_BANK_TRANSFER));

    paymentFacade.creditAccount(
        2L,
        new PaymentAccountDepositRequest(
            BigDecimal.valueOf(100_000), PaymentEventType.CHARGE_BANK_TRANSFER));

    paymentFacade.creditAccount(
        3L,
        new PaymentAccountDepositRequest(
            BigDecimal.valueOf(100_000), PaymentEventType.CHARGE_BANK_TRANSFER));

    paymentFacade.creditAccount(
        4L,
        new PaymentAccountDepositRequest(
            BigDecimal.valueOf(20_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        5L,
        new PaymentAccountDepositRequest(
            BigDecimal.valueOf(150_000), PaymentEventType.CHARGE_BANK_TRANSFER));
    paymentFacade.creditAccount(
        6L,
        new PaymentAccountDepositRequest(
            BigDecimal.valueOf(50_000), PaymentEventType.CHARGE_BANK_TRANSFER));
  }
}
