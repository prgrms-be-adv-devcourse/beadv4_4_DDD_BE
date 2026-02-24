package com.modeunsa.client;

import com.modeunsa.dto.ApiResponse;
import com.modeunsa.dto.AuthStatusResponse;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

@Slf4j
@Component
public class AuthServiceClient {
  private final WebClient webClient;

  public AuthServiceClient(@Value("${services.member.host:localhost}") String memberHost) {
    String baseUrl = "http://" + memberHost + ":8086";

    // 타임아웃 설정 (HttpClient 구성)
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000) // 연결 타임아웃 3초
            .responseTimeout(Duration.ofSeconds(3)) // 응답 타임아웃 3초
            .doOnConnected(
                conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(3)) // 읽기 타임아웃 3초
                        .addHandlerLast(new WriteTimeoutHandler(3))); // 쓰기 타임아웃 3초

    // WebClient에 HttpClient 적용
    this.webClient =
        WebClient.builder()
            .baseUrl(baseUrl)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
  }

  public Mono<AuthStatusResponse> validateToken(String accessToken) {
    return webClient
        .get()
        .uri("/api/v1/auths/me")
        .header("Authorization", "Bearer " + accessToken)
        .retrieve()
        // 에러 처리 (상태 코드별 로깅)
        .onStatus(
            HttpStatusCode::is4xxClientError,
            response -> {
              log.warn("인증 실패 (4xx): 상태 코드 {}", response.statusCode());
              return response.createException().flatMap(Mono::error);
            })
        .onStatus(
            HttpStatusCode::is5xxServerError,
            response -> {
              log.error("멤버 서비스 장애 (5xx): 상태 코드 {}", response.statusCode());
              return response.createException().flatMap(Mono::error);
            })
        .bodyToMono(new ParameterizedTypeReference<ApiResponse<AuthStatusResponse>>() {})
        .map(ApiResponse::getResult)
        // 재시도 메커니즘
        .retryWhen(
            Retry.backoff(3, Duration.ofMillis(500)) // 최대 3번, 500ms부터 시작하여 점점 길게 대기하며 재시도
                .filter(
                    throwable -> {
                      // 4xx 에러(예: 잘못된 토큰)는 재시도해봤자 계속 실패하므로 제외.
                      // 5xx 서버 에러나 타임아웃/네트워크 오류만 재시도 진행.
                      if (throwable instanceof WebClientResponseException) {
                        return ((WebClientResponseException) throwable)
                            .getStatusCode()
                            .is5xxServerError();
                      }
                      return true;
                    })
                .onRetryExhaustedThrow(
                    (retryBackoffSpec, retrySignal) -> {
                      log.error("멤버 서비스 요청 재시도 한도 초과: ", retrySignal.failure());
                      return retrySignal.failure();
                    }))
        .doOnError(e -> log.error("Token validation process failed completely", e));
  }
}
