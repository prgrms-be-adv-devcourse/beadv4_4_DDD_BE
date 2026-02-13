package com.modeunsa.boundedcontext.payment.in.api.v2;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookHeaders;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "Payment", description = "결제 도메인 API")
@RestController("PaymentV2Controller")
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentFacade paymentFacade;
  private final JsonConverter jsonConverter;

  @Operation(summary = "토스 웹훅", description = "토스 웹훅을 처리하는 API 입니다.")
  @PostMapping("/webhooks/toss")
  public ResponseEntity<ApiResponse> receiveTossWebHook(
      @RequestHeader(TossWebhookHeaders.TOSS_TRANSMISSION_ID) String transmissionId,
      @RequestHeader(TossWebhookHeaders.TOSS_TRANSMISSION_TIME) OffsetDateTime transmissionTime,
      @RequestHeader(TossWebhookHeaders.TOSS_RETRY_COUNT) int retryCount,
      @RequestBody String rawBody) {

    log.info("[{}] Receive Toss Webhook Request: {}", transmissionId, rawBody);

    TossWebhookRequest tossWebhookRequest =
        jsonConverter.deserialize(rawBody, TossWebhookRequest.class);

    paymentFacade.handleTossWebhookEvent(
        transmissionId, transmissionTime, retryCount, tossWebhookRequest);

    return ApiResponse.onSuccess(SuccessStatus.OK);
  }
}
