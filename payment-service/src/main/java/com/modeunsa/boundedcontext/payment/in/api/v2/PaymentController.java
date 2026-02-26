package com.modeunsa.boundedcontext.payment.in.api.v2;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentListItemResponse;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentSearchRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookHeaders;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossWebhookRequest;
import com.modeunsa.global.json.JsonConverter;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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

  @Operation(summary = "결제 목록 조회", description = "조회 기간, 결제 상태, 주문 번호로 결제 내역을 검색합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse> getPaymentList(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid PaymentSearchRequest paymentSearchRequest) {
    Page<PaymentListItemResponse> page =
        paymentFacade.getPaymentListPage(user.getMemberId(), paymentSearchRequest);
    return ApiResponse.onSuccess(SuccessStatus.OK, page);
  }

  @Operation(summary = "토스 웹훅", description = "토스 웹훅을 처리하는 API 입니다.")
  @PostMapping("/webhooks/toss")
  public ResponseEntity<ApiResponse> receiveTossWebHook(
      @RequestHeader(TossWebhookHeaders.TOSS_TRANSMISSION_ID) String transmissionId,
      @RequestHeader(TossWebhookHeaders.TOSS_TRANSMISSION_TIME) OffsetDateTime transmissionTime,
      @RequestHeader(TossWebhookHeaders.TOSS_RETRY_COUNT) int retryCount,
      @RequestBody String rawBody) {

    try {
      TossWebhookRequest tossWebhookRequest =
          jsonConverter.deserialize(rawBody, TossWebhookRequest.class);

      paymentFacade.handleTossWebhookEvent(
          transmissionId, transmissionTime, retryCount, tossWebhookRequest, rawBody);
    } catch (RuntimeException e) {
      log.error(
          "Failed to process Toss webhook. transmissionId={}, transmissionTime={}, retryCount={}, "
              + "rawBody={}, error={}",
          transmissionId,
          transmissionTime,
          retryCount,
          rawBody,
          e.getMessage(),
          e);
    }

    return ApiResponse.onSuccess(SuccessStatus.OK);
  }
}
