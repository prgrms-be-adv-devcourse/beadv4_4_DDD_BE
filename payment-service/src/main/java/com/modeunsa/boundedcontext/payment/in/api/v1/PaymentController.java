package com.modeunsa.boundedcontext.payment.in.api.v1;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.payment.ConfirmPaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.payment.ConfirmPaymentResponse;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentResponse;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.security.CustomUserDetails;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 도메인 API")
@RestController("PaymentV1Controller")
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentFacade paymentFacade;

  @Operation(summary = "결제 요청 기능", description = "결제를 요청하는 기능입니다.")
  @PostMapping
  public ResponseEntity<ApiResponse> requestPayment(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody PaymentRequest paymentRequest) {
    PaymentResponse response = paymentFacade.requestPayment(user, paymentRequest);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }

  @Operation(summary = "결제 승인 요청", description = "토스페이먼츠로부터 결제 승인을 요청하는 기능입니다.")
  @PostMapping("/{orderNo}/payment/confirm/by/tossPayments")
  public ResponseEntity<ApiResponse> confirmPaymentByTossPayments(
      @AuthenticationPrincipal CustomUserDetails user,
      @PathVariable String orderNo,
      @Valid @RequestBody ConfirmPaymentRequest confirmPaymentRequest) {
    ConfirmPaymentResponse response =
        paymentFacade.confirmTossPayment(user, orderNo, confirmPaymentRequest);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
