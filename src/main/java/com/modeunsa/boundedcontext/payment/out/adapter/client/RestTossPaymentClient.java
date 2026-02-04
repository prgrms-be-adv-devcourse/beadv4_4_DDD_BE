package com.modeunsa.boundedcontext.payment.out.adapter.client;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_INVALID_REQUEST_TOSS_API;
import static com.modeunsa.global.status.ErrorStatus.PAYMENT_REJECT_TOSS_PAYMENT;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
import com.modeunsa.global.exception.GeneralException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class RestTossPaymentClient implements TossPaymentClient {

  private RestClient tossRestClient;
  private final ObjectMapper objectMapper;

  @Value("${payment.toss.base-url:}")
  private String tossBaseUrl;

  @Value("${payment.toss.confirm-path:}")
  private String confirmPath;

  @Value("${payment.toss.secret-key:}")
  private String tossSecretKey;

  public RestTossPaymentClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    if (tossBaseUrl == null || tossBaseUrl.isBlank()) {
      throw new IllegalStateException("payment.toss.base-url이 설정되지 않았습니다.");
    }
    this.tossRestClient = RestClient.builder().baseUrl(tossBaseUrl).build();
  }

  @Override
  public TossPaymentsConfirmResponse confirmPayment(
      TossPaymentsConfirmRequest tossPaymentsConfirmRequest) {
    String paymentKey = tossPaymentsConfirmRequest.paymentKey();
    String orderId = tossPaymentsConfirmRequest.orderId();
    long amount = tossPaymentsConfirmRequest.amount();

    log.debug(
        "[토스페이먼츠 결제 승인 요청 시작] paymentKey: {}, orderId: {}, amount: {}",
        paymentKey,
        orderId,
        amount);

    try {
      ResponseEntity<Map<String, Object>> responseEntity =
          createConfirmRequest(tossPaymentsConfirmRequest)
              .retrieve()
              .toEntity(new ParameterizedTypeReference<>() {});

      int httpStatus = responseEntity.getStatusCode().value();
      Map<String, Object> responseBody = responseEntity.getBody();

      log.debug("[토스페이먼츠 결제 승인 응답] httpStatus: {}, responseBody: {}", httpStatus, responseBody);

      if (httpStatus != 200) {
        log.warn(
            "[토스페이먼츠 결제 승인 실패] httpStatus: {}, paymentKey: {}, orderId: {}",
            httpStatus,
            paymentKey,
            orderId);
        throw createDomainExceptionFromNon200(httpStatus, responseBody);
      }

      if (responseBody == null) {
        log.error("[토스페이먼츠 결제 승인 응답 null] paymentKey: {}, orderId: {}", paymentKey, orderId);
        throw new GeneralException(PAYMENT_INVALID_REQUEST_TOSS_API);
      }

      log.info(
          "[토스페이먼츠 결제 승인 성공] paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);
      return objectMapper.convertValue(responseBody, TossPaymentsConfirmResponse.class);
    } catch (RestClientResponseException e) {
      log.error(
          "[토스페이먼츠 결제 승인 RestClientResponseException] "
              + "paymentKey: {}, orderId: {}, status: {}, body: {}",
          paymentKey,
          orderId,
          e.getStatusCode(),
          e.getResponseBodyAsString(StandardCharsets.UTF_8),
          e);
      throw createDomainExceptionFromHttpError(e);
    } catch (GeneralException e) {
      log.error(
          "[토스페이먼츠 결제 승인 GeneralException] paymentKey: {}, orderId: {}, error: {}",
          paymentKey,
          orderId,
          e.getMessage(),
          e);
      throw e;
    } catch (Exception e) {
      log.error(
          "[토스페이먼츠 결제 승인 예외 발생] paymentKey: {}, orderId: {}, error: {}",
          paymentKey,
          orderId,
          e.getMessage(),
          e);
      throw new GeneralException(PAYMENT_REJECT_TOSS_PAYMENT);
    }
  }

  private RestClient.RequestHeadersSpec<?> createConfirmRequest(
      TossPaymentsConfirmRequest tossPaymentsConfirmRequest) {
    return tossRestClient
        .post()
        .uri(confirmPath)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth(tossSecretKey, ""))
        .body(tossPaymentsConfirmRequest);
  }

  private GeneralException createDomainExceptionFromNon200(int httpStatus, Map responseBody) {
    if (responseBody == null) {
      return new GeneralException(PAYMENT_INVALID_REQUEST_TOSS_API);
    }

    @SuppressWarnings("unchecked")
    Map<String, Object> body = (Map<String, Object>) responseBody;

    String tossCode = extractStringOrDefault(body, "code", "HTTP_" + httpStatus);
    String tossMessage = extractStringOrDefault(body, "message", "토스 결제 승인 실패, HTTP " + httpStatus);
    log.error("response api code: {}, message: {}", tossCode, tossMessage);
    return new GeneralException(PAYMENT_REJECT_TOSS_PAYMENT);
  }

  private GeneralException createDomainExceptionFromHttpError(RestClientResponseException e) {
    int httpStatus = e.getStatusCode().value();
    String rawBody = e.getResponseBodyAsString(StandardCharsets.UTF_8);

    if (rawBody.isBlank()) {
      return new GeneralException(PAYMENT_INVALID_REQUEST_TOSS_API);
    }

    try {
      Map<String, Object> errorBody = objectMapper.readValue(rawBody, new TypeReference<>() {});
      String tossCode = extractStringOrDefault(errorBody, "code", "HTTP_" + httpStatus);
      String tossMessage =
          extractStringOrDefault(errorBody, "message", "토스 결제 승인 실패, HTTP " + httpStatus);
      log.error("reject toss payment approve, api code: {}, message: {}", tossCode, tossMessage);
      return new GeneralException(PAYMENT_INVALID_REQUEST_TOSS_API);
    } catch (Exception parseFail) {
      return new GeneralException(PAYMENT_INVALID_REQUEST_TOSS_API);
    }
  }

  private String extractStringOrDefault(Map<String, Object> map, String key, String defaultValue) {
    Object value = map.get(key);
    if (value instanceof String s && !s.isBlank()) {
      return s;
    }
    return defaultValue;
  }
}
