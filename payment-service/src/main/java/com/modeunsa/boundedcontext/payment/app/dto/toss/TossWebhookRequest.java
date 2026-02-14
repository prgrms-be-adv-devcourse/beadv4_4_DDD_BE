package com.modeunsa.boundedcontext.payment.app.dto.toss;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public record TossWebhookRequest(
    @NotBlank String eventType, @NotNull LocalDateTime createdAt, @NotNull TossWebhookData data) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record TossWebhookData(
      String version,
      String lastTransactionKey,
      String paymentKey,
      String orderId,
      TossPaymentStatus status,
      OffsetDateTime requestedAt,
      OffsetDateTime approvedAt,
      Card card,
      List<CancelInfo> cancels,
      FailureInfo failure) {

    public enum TossPaymentStatus {
      READY,
      IN_PROGRESS,
      WAITING_FOR_DEPOSIT,
      DONE,
      CANCELED,
      PARTIAL_CANCELED,
      ABORTED,
      EXPIRED
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(
        String issuerCode,
        String acquirerCode,
        String number,
        int installmentPlanMonths,
        long amount) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CancelInfo(
        long cancelAmount, String cancelReason, OffsetDateTime canceledAt, String transactionKey) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FailureInfo(String code, String message) {}
  }
}
