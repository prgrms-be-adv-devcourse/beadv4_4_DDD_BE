package com.modeunsa.boundedcontext.payment.in.v1;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentRequest;
import com.modeunsa.boundedcontext.payment.app.dto.payment.PaymentResponse;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentPurpose;
import com.modeunsa.boundedcontext.payment.domain.types.ProviderType;
import com.modeunsa.boundedcontext.payment.in.BasePaymentControllerTest;
import com.modeunsa.boundedcontext.payment.in.api.v1.PaymentController;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import com.modeunsa.shared.member.MemberRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

@DisplayName("PaymentController 테스트")
class PaymentControllerTest extends BasePaymentControllerTest {

  @Mock private PaymentFacade paymentFacade;

  @BeforeEach
  void setUp() {
    super.setUpBase();
    setUpMockMvc(new PaymentController(paymentFacade));
    setSecurityContext(1L, MemberRole.MEMBER, null);
  }

  @Test
  @DisplayName("결제 요청 성공 - 유효한 요청으로 결제 요청 성공")
  void requestPaymentSuccess() throws Exception {
    // given
    Long orderId = 1L;
    String orderNo = "ORDER12345";
    Long buyerId = 1000L;
    BigDecimal totalAmount = BigDecimal.valueOf(50000);
    LocalDateTime paymentDeadlineAt = LocalDateTime.now().plusDays(1);

    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(orderId)
            .orderNo(orderNo)
            .totalAmount(totalAmount)
            .paymentDeadlineAt(paymentDeadlineAt)
            .providerType(ProviderType.MODEUNSA_PAY)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
            .build();

    PaymentResponse response =
        PaymentResponse.builder()
            .buyerId(buyerId)
            .orderNo(orderNo)
            .orderId(orderId)
            .totalAmount(totalAmount)
            .needsPgPayment(false)
            .requestPgAmount(BigDecimal.ZERO)
            .build();

    when(paymentFacade.requestPayment(any(), any())).thenReturn(response);

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
            .paymentDeadlineAt(LocalDateTime.now().plusDays(1))
            .providerType(ProviderType.MODEUNSA_PAY)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
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
  @DisplayName("결제 요청 실패 - totalAmount가 0 이하인 경우")
  void requestPaymentFailureTotalAmountInvalid() throws Exception {
    // given
    PaymentRequest request =
        PaymentRequest.builder()
            .orderId(1L)
            .orderNo("ORDER12345")
            .totalAmount(BigDecimal.ZERO)
            .paymentDeadlineAt(LocalDateTime.now().plusDays(1))
            .providerType(ProviderType.MODEUNSA_PAY)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
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
            .paymentDeadlineAt(LocalDateTime.now().plusDays(1))
            .providerType(ProviderType.MODEUNSA_PAY)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
            .build();

    when(paymentFacade.requestPayment(any(), any()))
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
            .paymentDeadlineAt(LocalDateTime.now().plusDays(1))
            .providerType(ProviderType.MODEUNSA_PAY)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
            .build();

    when(paymentFacade.requestPayment(any(), any()))
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
            .paymentDeadlineAt(LocalDateTime.now().plusDays(1))
            .providerType(ProviderType.MODEUNSA_PAY)
            .paymentPurpose(PaymentPurpose.PRODUCT_PURCHASE)
            .build();

    when(paymentFacade.requestPayment(any(), any()))
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
