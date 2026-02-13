package com.modeunsa.boundedcontext.payment.app.usecase.webhook;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest.TossWebhookData;
import com.modeunsa.boundedcontext.payment.app.support.PaymentSupport;
import com.modeunsa.boundedcontext.payment.domain.entity.Payment;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookErrorCode;
import com.modeunsa.boundedcontext.payment.domain.exception.TossWebhookException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TossWebhookUseCase {

  private final PaymentSupport paymentSupport;

  public void execute(@NotNull TossWebhookData data) {

    Payment payment = paymentSupport.getPaymentByOrderNo(data.orderId());

    switch (data.status()) {
      case READY, WAITING_FOR_DEPOSIT -> System.out.println("Waiting for deposit");
      case IN_PROGRESS -> System.out.println("Waiting for deposit");
      case DONE -> System.out.println("Payment completed");
      case CANCELED, PARTIAL_CANCELED -> System.out.println("Payment canceled");
      case ABORTED -> System.out.println("Payment aborted");
      case EXPIRED -> System.out.println("Payment expired");
      default -> throw new TossWebhookException(TossWebhookErrorCode.INVALID_EVENT_TYPE);
    }
  }
}
