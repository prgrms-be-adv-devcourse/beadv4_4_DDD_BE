package com.modeunsa.boundedcontext.payment.out.client;

import static com.modeunsa.global.status.ErrorStatus.PAYMENT_INVALID_REQUEST_TOSS_API;
import static com.modeunsa.global.status.ErrorStatus.PAYMENT_REJECT_TOSS_PAYMENT;

import com.modeunsa.boundedcontext.payment.app.dto.ConfirmPaymentRequest;
import com.modeunsa.global.exception.GeneralException;
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

  private final RestClient tossRestClient;
  private final ObjectMapper objectMapper;

  @Value("${payment.toss.base-url:}")
  private String tossBaseUrl;

  @Value("${payment.toss.confirm-path:}")
  private String confirmPath;

  @Value("${payment.toss.secret-key:}")
  private String tossSecretKey;

  public RestTossPaymentClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.tossRestClient = RestClient.builder().baseUrl(tossBaseUrl).build();
  }

  @Override
  public Map<String, Object> confirmPayment(
      String orderNo, ConfirmPaymentRequest confirmPaymentRequest) {
    try {

      ResponseEntity<Map<String, Object>> responseEntity =
          createConfirmRequest(confirmPaymentRequest)
              .retrieve()
              .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

      int httpStatus = responseEntity.getStatusCode().value();
      Map<String, Object> responseBody = responseEntity.getBody();

      if (httpStatus != 200) {
        throw createDomainExceptionFromNon200(httpStatus, responseBody);
      }

      if (responseBody == null) {
        throw new GeneralException(PAYMENT_INVALID_REQUEST_TOSS_API);
      }

      @SuppressWarnings("unchecked")
      Map<String, Object> casted = (Map<String, Object>) responseBody;
      return casted;
    } catch (RestClientResponseException e) {
      throw createDomainExceptionFromHttpError(e);
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(PAYMENT_REJECT_TOSS_PAYMENT);
    }
  }

  private RestClient.RequestHeadersSpec<?> createConfirmRequest(
      ConfirmPaymentRequest confirmPaymentRequest) {
    return tossRestClient
        .post()
        .uri(confirmPath)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .headers(headers -> headers.setBasicAuth(tossSecretKey, ""))
        .body(confirmPaymentRequest);
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
