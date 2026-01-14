package com.modeunsa.boundedcontext.payment.in;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.boundedcontext.payment.app.PaymentFacade;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositRequest;
import com.modeunsa.boundedcontext.payment.app.dto.PaymentAccountDepositResponse;
import com.modeunsa.boundedcontext.payment.domain.types.PaymentEventType;
import com.modeunsa.global.exception.ExceptionAdvice;
import com.modeunsa.global.exception.GeneralException;
import com.modeunsa.global.status.ErrorStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@DisplayName("PaymentAccountController 테스트")
class ApiV1PaymentAccountControllerTest {

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock private PaymentFacade paymentFacade;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    objectMapper = new ObjectMapper();
    ApiV1PaymentAccountController controller = new ApiV1PaymentAccountController(paymentFacade);
    LocalValidatorFactoryBean validatorFactory = new LocalValidatorFactoryBean();
    validatorFactory.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validatorFactory)
            .setControllerAdvice(new ExceptionAdvice())
            .build();
  }

  @Test
  @DisplayName("계좌 입금 성공 - 유효한 요청으로 입금 성공")
  void depositAccountSuccess() throws Exception {
    // given
    Long memberId = 1L;
    long amount = 10000L;
    PaymentEventType eventType = PaymentEventType.CHARGE_BANK_TRANSFER;
    BigDecimal balanceAfter = new BigDecimal("10000.00");

    PaymentAccountDepositRequest request =
        new PaymentAccountDepositRequest(memberId, amount, eventType);
    PaymentAccountDepositResponse response = new PaymentAccountDepositResponse(balanceAfter);

    when(paymentFacade.creditAccount(any(PaymentAccountDepositRequest.class))).thenReturn(response);

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
    Long memberId = 999L;
    long amount = 10000L;
    PaymentEventType eventType = PaymentEventType.CHARGE_BANK_TRANSFER;

    PaymentAccountDepositRequest request =
        new PaymentAccountDepositRequest(memberId, amount, eventType);

    when(paymentFacade.creditAccount(any(PaymentAccountDepositRequest.class)))
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
    Long memberId = 999L;
    long amount = 0;
    PaymentEventType eventType = PaymentEventType.CHARGE_BANK_TRANSFER;

    PaymentAccountDepositRequest request =
        new PaymentAccountDepositRequest(memberId, amount, eventType);

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
}
