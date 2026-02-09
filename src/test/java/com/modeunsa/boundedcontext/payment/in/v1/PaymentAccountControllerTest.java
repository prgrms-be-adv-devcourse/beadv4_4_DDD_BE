package com.modeunsa.boundedcontext.payment.in.v1;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.account.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.account.PaymentAccountDepositResponse;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberDto;
import com.modeunsa.boundedcontext.payment.app.dto.member.PaymentMemberResponse;
import com.modeunsa.boundedcontext.payment.app.mapper.PaymentMapper;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.boundedcontext.payment.in.BasePaymentControllerTest;
import com.modeunsa.boundedcontext.payment.in.api.v1.PaymentAccountController;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.MediaType;

@DisplayName("PaymentAccountController 테스트")
class PaymentAccountControllerTest extends BasePaymentControllerTest {

  @Mock private PaymentFacade paymentFacade;
  @Mock private PaymentMapper paymentMapper;

  @BeforeEach
  void setUp() {
    super.setUpBase();
    setUpMockMvc(new PaymentAccountController(paymentFacade, paymentMapper));
    setSecurityContext(1L, MemberRole.MEMBER, null);
  }

  @Test
  @DisplayName("계좌 입금 성공 - 유효한 요청으로 입금 성공")
  void depositAccountSuccess() throws Exception {
    // given
    Long memberId = 1L;
    BigDecimal amount = BigDecimal.valueOf(10000.00);
    PaymentEventType eventType = PaymentEventType.CHARGE_BANK_TRANSFER;
    BigDecimal balanceAfter = BigDecimal.valueOf(10000.00);

    PaymentAccountDepositRequest request = new PaymentAccountDepositRequest(amount, eventType);
    PaymentAccountDepositResponse response = new PaymentAccountDepositResponse(balanceAfter);

    when(paymentFacade.creditAccount(memberId, request)).thenReturn(response);

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isSuccess").value(true))
        .andExpect(jsonPath("$.result.balance").value(10000L));
  }

  @Test
  @DisplayName("계좌 입금 실패 - 계좌를 찾을 수 없는 경우")
  void depositAccountFailureAccountNotFound() throws Exception {
    // given
    Long memberId = 1L;
    BigDecimal amount = BigDecimal.valueOf(10000.00);
    PaymentEventType eventType = PaymentEventType.CHARGE_BANK_TRANSFER;

    PaymentAccountDepositRequest request = new PaymentAccountDepositRequest(amount, eventType);

    when(paymentFacade.creditAccount(memberId, request))
        .thenThrow(new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("계좌 입금 실패 - 입금액이 0원인 경우")
  void depositAccountFailureZeroAmount() throws Exception {
    // given
    BigDecimal amount = BigDecimal.ZERO;
    PaymentEventType eventType = PaymentEventType.CHARGE_BANK_TRANSFER;

    PaymentAccountDepositRequest request = new PaymentAccountDepositRequest(amount, eventType);

    // when, then
    mockMvc
        .perform(
            post("/api/v1/payments/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  @DisplayName("계좌 정보 조회 성공 - 유효한 요청으로 계좌 정보 조회 성공")
  void getMemberSuccess() throws Exception {
    // given
    Long memberId = 1L;
    String customerKey = "customer_key_123";
    String customerName = "홍길동";
    String customerEmail = "test@example.com";
    BigDecimal balance = BigDecimal.valueOf(50000.00);

    PaymentMemberDto dto = new PaymentMemberDto(customerKey, customerName, customerEmail, balance);

    PaymentMemberResponse response =
        new PaymentMemberResponse(customerKey, customerName, customerEmail, balance);

    when(paymentFacade.getMember(memberId)).thenReturn(dto);
    when(paymentMapper.toPaymentMemberResponse(dto)).thenReturn(response);

    // when, then
    mockMvc
        .perform(get("/api/v1/payments/accounts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isSuccess").value(true))
        .andExpect(jsonPath("$.result.customerKey").value(customerKey))
        .andExpect(jsonPath("$.result.customerName").value(customerName))
        .andExpect(jsonPath("$.result.customerEmail").value(customerEmail))
        .andExpect(jsonPath("$.result.balance").value(50000));
  }

  @Test
  @DisplayName("계좌 정보 조회 실패 - 계좌를 찾을 수 없는 경우")
  void getMemberFailureAccountNotFound() throws Exception {
    // given
    Long memberId = 1L;

    when(paymentFacade.getMember(memberId))
        .thenThrow(new GeneralException(ErrorStatus.PAYMENT_ACCOUNT_NOT_FOUND));

    // when, then
    mockMvc
        .perform(get("/api/v1/payments/accounts"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.isSuccess").value(false))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.message").exists());
  }
}
