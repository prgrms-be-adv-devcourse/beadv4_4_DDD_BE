package com.modeunsa.client;

import com.modeunsa.dto.ApiResponse;
import com.modeunsa.dto.AuthStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class MemberServiceClient {
  private final WebClient webClient;

  public MemberServiceClient(
    @Value("${services.member.host:localhost}") String memberHost) {
    String baseUrl = "http://" + memberHost + ":8086";

    this.webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build();
  }

  public Mono<AuthStatusResponse> validateToken(String accessToken) {
    return webClient.get()
        .uri("/api/v1/auths/me")
        .header("Authorization", "Bearer " + accessToken)
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<ApiResponse<AuthStatusResponse>>() {})
        .map(ApiResponse::getResult);
  }
}