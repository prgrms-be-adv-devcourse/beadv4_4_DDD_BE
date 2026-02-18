package com.modeunsa.boundedcontext.payment.out.client;

import static com.modeunsa.boundedcontext.payment.domain.exception.PaymentErrorCode.PG_TOSS_CONFIRM_FAILED;

import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmRequest;
import com.modeunsa.boundedcontext.payment.app.dto.toss.TossPaymentsConfirmResponse;
import com.modeunsa.boundedcontext.payment.domain.exception.TossConfirmFailedException;
import com.modeunsa.boundedcontext.payment.domain.exception.TossConfirmRetryableException;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
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

  @Value("${payment.toss.connect-timeout-seconds:3}")
  private Duration connectTimeout;

  @Value("${payment.toss.read-timeout-seconds:10}")
  private Duration readTimeout;

  public RestTossPaymentClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @PostConstruct
  public void init() {
    if (tossBaseUrl == null || tossBaseUrl.isBlank()) {
      throw new IllegalStateException("payment.toss.base-url이 설정되지 않았습니다.");
    }
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(connectTimeout);
    factory.setReadTimeout(readTimeout);
    this.tossRestClient = RestClient.builder().baseUrl(tossBaseUrl).requestFactory(factory).build();
  }

  /**
   * Toss PG 전용 재시도 정책: 재시도 가능 예외만 N회 재시도, 그 외는 즉시 실패. - 재시도: {@link TossConfirmRetryableException}
   * (502/503/504), {@link ResourceAccessException} (네트워크/타임아웃) - 재시도 안 함: {@link
   * TossConfirmFailedException} (4xx, 500 등)
   */
  @Override
  @Retryable(
      retryFor = {TossConfirmRetryableException.class, ResourceAccessException.class},
      noRetryFor = {TossConfirmFailedException.class},
      maxAttempts = 3,
      backoff = @Backoff(delay = 500, multiplier = 2))
  public TossPaymentsConfirmResponse confirmPayment(
      TossPaymentsConfirmRequest tossPaymentsConfirmRequest) {
    String paymentKey = tossPaymentsConfirmRequest.paymentKey();
    String orderId = tossPaymentsConfirmRequest.orderId();
    long amount = tossPaymentsConfirmRequest.amount();

    log.debug(
        "[토스페이먼츠 결제 승인 요청] paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);

    try {
      ResponseEntity<Map<String, Object>> responseEntity =
          createConfirmRequest(tossPaymentsConfirmRequest)
              .retrieve()
              .toEntity(new ParameterizedTypeReference<>() {});

      int status = responseEntity.getStatusCode().value();
      Map<String, Object> body = responseEntity.getBody();

      log.debug("[토스페이먼츠 결제 승인 응답] httpStatus: {}, responseBody: {}", status, body);

      if (status != 200) {
        throw createTossConfirmException(status, body);
      }
      if (body == null) {
        throw new TossConfirmFailedException(PG_TOSS_CONFIRM_FAILED, "토스 결제 승인 실패, 응답 바디가 없습니다.");
      }

      log.debug(
          "[토스페이먼츠 결제 승인 성공] paymentKey: {}, orderId: {}, amount: {}", paymentKey, orderId, amount);
      return objectMapper.convertValue(body, TossPaymentsConfirmResponse.class);
    } catch (RestClientResponseException e) {
      throw createTossConfirmExceptionFromHttpError(e);
    } catch (TossConfirmFailedException
        | TossConfirmRetryableException
        | ResourceAccessException e) {
      throw e;
    } catch (Exception e) {
      log.error(
          "[토스페이먼츠 결제 승인 예외 발생] paymentKey: {}, orderId: {}, error: {}",
          paymentKey,
          orderId,
          e.getMessage(),
          e);
      throw new TossConfirmFailedException(PG_TOSS_CONFIRM_FAILED, e.getMessage());
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

  private RuntimeException createTossConfirmException(int httpStatus, Map<String, Object> body) {
    TossErrorDetail detail = parseTossError(httpStatus, body);
    if (isRetryableHttpStatus(httpStatus)) {
      return new TossConfirmRetryableException(detail.message());
    }
    return new TossConfirmFailedException(
        PG_TOSS_CONFIRM_FAILED, detail.code(), detail.message(), detail.message());
  }

  private RuntimeException createTossConfirmExceptionFromHttpError(RestClientResponseException e) {
    int httpStatus = e.getStatusCode().value();
    String rawBody = e.getResponseBodyAsString(StandardCharsets.UTF_8);

    if (rawBody.isBlank()) {
      TossErrorDetail detail = parseTossError(httpStatus, null);
      return new TossConfirmFailedException(
          PG_TOSS_CONFIRM_FAILED, detail.code(), detail.message(), detail.message());
    }

    try {
      Map<String, Object> body = objectMapper.readValue(rawBody, new TypeReference<>() {});
      TossErrorDetail detail = parseTossError(httpStatus, body);
      log.error("[토스페이먼츠 결제 승인 실패 code:{}, message:{}]", detail.code(), detail.message());
      if (isRetryableHttpStatus(httpStatus)) {
        return new TossConfirmRetryableException(detail.message());
      }
      return new TossConfirmFailedException(
          PG_TOSS_CONFIRM_FAILED, detail.code(), detail.message(), detail.message());
    } catch (Exception parseFail) {
      TossErrorDetail detail = parseTossError(httpStatus, null);
      String message = detail.message() + ", 응답 바디 파싱 실패: " + parseFail.getMessage();
      return new TossConfirmFailedException(
          PG_TOSS_CONFIRM_FAILED, detail.code(), message, message);
    }
  }

  private boolean isRetryableHttpStatus(int httpStatus) {
    return httpStatus == 502 || httpStatus == 503 || httpStatus == 504;
  }

  private String extractStringOrDefault(Map<String, Object> map, String key, String defaultValue) {
    Object value = map.get(key);
    if (value instanceof String s && !s.isBlank()) {
      return s;
    }
    return defaultValue;
  }

  private TossErrorDetail parseTossError(int httpStatus, Map<String, Object> body) {
    String defaultCode = "HTTP_" + httpStatus;
    String defaultMessage = "토스 결제 승인 실패, HTTP " + httpStatus;
    if (body == null) {
      return new TossErrorDetail(defaultCode, defaultMessage);
    }
    return new TossErrorDetail(
        extractStringOrDefault(body, "code", defaultCode),
        extractStringOrDefault(body, "message", defaultMessage));
  }

  private record TossErrorDetail(String code, String message) {}
}
