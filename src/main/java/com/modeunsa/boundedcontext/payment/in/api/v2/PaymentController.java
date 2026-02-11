package com.modeunsa.boundedcontext.payment.in.api.v2;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookHeaders;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 도메인 API")
@RestController("PaymentV2Controller")
@RequestMapping("/api/v2/payments")
@RequiredArgsConstructor
public class PaymentController {

  @Operation(summary = "토스 웹훅", description = "토스 웹훅을 처리하는 API 입니다.")
  @PostMapping("/webhooks/toss")
  public ResponseEntity<ApiResponse> receiveTossWebHook(
      @RequestHeader(TossWebhookHeaders.TOSS_TRANSMISSION_ID) String transmissionId,
      @RequestHeader(TossWebhookHeaders.TOSS_TRANSMISSION_TIME) String transmissionTime,
      @RequestHeader(TossWebhookHeaders.TOSS_RETRY_COUNT) int retryCount,
      @RequestBody @Valid TossWebhookRequest tossWebhookRequest) {
    return ApiResponse.onSuccess(SuccessStatus.OK);
  }
}
