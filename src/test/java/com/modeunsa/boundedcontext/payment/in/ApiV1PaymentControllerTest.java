package com.modeunsa.boundedcontext.payment.in;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentResponse;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

@DisplayName("PaymentController 테스트")
class ApiV1PaymentControllerTest extends BasePaymentControllerTest {

  @Mock private PaymentFacade paymentFacade;

  @BeforeEach
  void setUp() {
    super.setUpBase();
    setUpMockMvc(new ApiV1PaymentController(paymentFacade));
  }

  @Test
  @DisplayName("결제 요청 성공 - 유효한 요청으로 결제 요청 성공")
  void requestPaymentSuccess() throws Exception {
    // given
    Long orderId = 1L;
    String orderNo = "ORDER12345";
    Long buyerId = 1000L;
    BigDecimal totalAmount = BigDecimal.valueOf(50000);

    PaymentRequest request =
        PaymentRequest.builder().orderId(orderId).orderNo(orderNo).totalAmount(totalAmount).build();

    PaymentResponse response =
        PaymentResponse.builder()
            .buyerId(buyerId)
            .orderNo(orderNo)
            .orderId(orderId)
            .totalAmount(totalAmount)
            .needsCharge(false)
            .chargeAmount(BigDecimal.ZERO)
            .build();

    // 컨트롤러에서는 @AuthenticationPrincipal Long memberId 를 사용하지만,
    // 테스트에서는 SecurityContext 를 구성하지 않으므로 null 이 전달된다.
    // memberId 값에 의존하지 않도록 any() 매처를 사용해 성공 응답을 스텁한다.
    when(paymentFacade.requestPayment(any(), any(PaymentRequest.class))).thenReturn(response);

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isSuccess").value(true))
        .andExpect(jsonPath("$.result.buyerId").value(1000L))
        .andExpect(jsonPath("$.result.orderNo").value("ORDER12345"))
        .andExpect(jsonPath("$.result.orderId").value(1L))
        .andExpect(jsonPath("$.result.totalAmount").value(50000));
  }

  @Test
  @DisplayName("결제 요청 실패 - orderId가 null인 경우")
  void requestPaymentFailureOrderIdNull() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(null)
            .orderNo("ORDER12345")
            .totalAmount(BigDecimal.valueOf(50000))
            .build();

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("결제 요청 실패 - orderNo가 빈 문자열인 경우")
  void requestPaymentFailureOrderNoEmpty() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("")
            .totalAmount(BigDecimal.valueOf(50000))
            .build();

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("결제 요청 실패 - buyerId가 null인 경우")
  void requestPaymentFailureBuyerIdNull() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .totalAmount(BigDecimal.valueOf(50000))
            .build();

    // 실제 구현에서는 인증 정보가 없으면 회원을 찾을 수 없다는 도메인 예외가 발생한다고 가정하고,
    // memberId 가 null 인 경우에 대한 예외 매핑을 검증한다.
    when(paymentFacade.requestPayment(isNull(), any(PaymentRequest.class)))
        .thenThrow(new GeneralException(ErrorStatus.PAYMENT_MEMBER_NOT_FOUND));

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("결제 요청 실패 - totalAmount가 0 이하인 경우")
  void requestPaymentFailureTotalAmountInvalid() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .totalAmount(BigDecimal.ZERO)
            .build();

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("결제 요청 실패 - 중복 결제인 경우")
  void requestPaymentFailureDuplicate() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .totalAmount(BigDecimal.valueOf(50000))
            .build();

    // memberId 값과 무관하게, 결제가 중복되었다는 도메인 예외가 발생한다고 가정한다.
    when(paymentFacade.requestPayment(any(), any(PaymentRequest.class)))
        .thenThrow(new GeneralException(ErrorStatus.PAYMENT_DUPLICATE));

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("결제 요청 실패 - 계좌를 찾을 수 없는 경우")
  void requestPaymentFailureAccountNotFound() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .totalAmount(BigDecimal.valueOf(50000))
            .build();

    // memberId 값과 무관하게, 계좌를 찾을 수 없다는 도메인 예외가 발생한다고 가정한다.
    when(paymentFacade.requestPayment(any(), any(PaymentRequest.class)))
        .thenThrow(new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("결제 요청 실패 - 회원 상태가 비활성인 경우")
  void requestPaymentFailureMemberInactive() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .totalAmount(BigDecimal.valueOf(50000))
            .build();

    // memberId 값과 무관하게, 회원 상태 비활성 예외가 발생한다고 가정한다.
    when(paymentFacade.requestPayment(any(), any(PaymentRequest.class)))
        .thenThrow(new GeneralException(ErrorStatus.PAYMENT_MEMBER_IN_ACTIVE));

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }
}
