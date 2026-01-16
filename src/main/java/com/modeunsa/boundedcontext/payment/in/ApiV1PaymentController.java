package com.modeunsa.boundedcontext.payment.in;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentResponse;
import com.modeunsa.global.response.ApiResponse;
import com.modeunsa.global.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment", description = "결제 도메인 API")
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class ApiV1PaymentController {

  private final PaymentFacade paymentFacade;

  @Operation(summary = "결제 요청 기능", description = "결제를 요청하는 기능입니다.")
  @PostMapping
  public ResponseEntity<ApiResponse> requestPayment(
      @Valid @RequestBody PaymentRequest paymentRequest) {
    PaymentResponse response = paymentFacade.requestPayment(paymentRequest);
    return ApiResponse.onSuccess(SuccessStatus.OK, response);
  }
}
