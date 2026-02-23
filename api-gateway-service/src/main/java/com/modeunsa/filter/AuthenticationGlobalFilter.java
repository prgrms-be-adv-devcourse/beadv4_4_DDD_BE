package com.modeunsa.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.client.AuthServiceClient;
import com.modeunsa.config.GatewaySecurityProperties;
import com.modeunsa.config.InternalProperties;
import com.modeunsa.dto.ApiResponse;
import com.modeunsa.dto.AuthStatusResponse;
import com.modeunsa.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

  private final AuthServiceClient authServiceClient;
  private final InternalProperties internalProperties;
  private final GatewaySecurityProperties securityProperties;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest originalRequest = exchange.getRequest();
    String path = originalRequest.getPath().value();

    // Header Injection 방지: 클라이언트가 보낸 내부 인증용 헤더 선제적 제거
    ServerHttpRequest cleanRequest =
        originalRequest
            .mutate()
            .headers(
                httpHeaders -> {
                  httpHeaders.remove("X-User-Id");
                  httpHeaders.remove("X-User-Role");
                  httpHeaders.remove("X-Seller-Id");
                })
            .build();

    ServerWebExchange cleanExchange = exchange.mutate().request(cleanRequest).build();

    // 1. /api/로 시작하지 않으면 통과
    if (!path.startsWith("/api/")) {
      return chain.filter(cleanExchange);
    }

    // 2. 공개 엔드포인트: JWT 있으면 검증 후 헤더 추가, 없으면 그대로 통과 (헤더는 null로 downstream 전달)
    if (isPublicEndpoint(path)) {
      return handlePublicEndpoint(cleanExchange, chain);
    }

    // 3. Internal API Key 체크
    String internalApiKeyHeader = cleanRequest.getHeaders().getFirst("X-INTERNAL-API-KEY");
    if (internalApiKeyHeader != null
        && internalApiKeyHeader.equals(internalProperties.getApiKey())) {
      return addInternalUserHeaders(cleanExchange, chain);
    }

    // 4. 일반 유저 토큰(JWT) 체크 (헤더와 쿠키 모두 확인)
    String accessToken = null;
    String authHeader = cleanRequest.getHeaders().getFirst("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      accessToken = authHeader.substring(7).trim();
    } else if (cleanRequest.getCookies().containsKey("accessToken")) {
      accessToken = cleanRequest.getCookies().getFirst("accessToken").getValue();
    }

    if (accessToken == null || accessToken.isBlank()) {
      return unauthorized(cleanExchange);
    }

    // 5. Member 서비스로 토큰 검증
    return authServiceClient
        .validateToken(accessToken)
        .flatMap(
            authStatus -> {
              if (!authStatus.isAuthenticated()) {
                return unauthorized(cleanExchange);
              }
              return addUserHeaders(cleanExchange, chain, authStatus);
            })
        .onErrorResume(
            e -> {
              log.error("토큰 검증 과정 중 오류 발생 - Path: {}, Message: {}", path, e.getMessage(), e);
              return unauthorized(cleanExchange);
            });
  }

  /**
   * 공개(permitAll) 경로: JWT가 있으면 검증 후 유효할 때만 X-User-Id 등 헤더 추가. JWT 없거나 무효면 헤더 없이 통과 → downstream에서
   * Principal은 null.
   */
  private Mono<Void> handlePublicEndpoint(
      ServerWebExchange cleanExchange, GatewayFilterChain chain) {
    ServerHttpRequest request = cleanExchange.getRequest();
    String accessToken = null;
    String authHeader = request.getHeaders().getFirst("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      accessToken = authHeader.substring(7).trim();
    } else if (request.getCookies().containsKey("accessToken")) {
      accessToken = request.getCookies().getFirst("accessToken").getValue();
    }
    if (accessToken == null || accessToken.isBlank()) {
      return chain.filter(cleanExchange);
    }
    return authServiceClient
        .validateToken(accessToken)
        .flatMap(
            authStatus -> {
              if (!authStatus.isAuthenticated()) {
                return chain.filter(cleanExchange);
              }
              return addUserHeaders(cleanExchange, chain, authStatus);
            })
        .onErrorResume(
            e -> {
              log.debug("공개 경로 JWT 검증 실패(무시하고 통과): {}", e.getMessage());
              return chain.filter(cleanExchange);
            });
  }

  private boolean isPublicEndpoint(String path) {
    return securityProperties.getPermitUrls().stream()
        .anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  private Mono<Void> addUserHeaders(
      ServerWebExchange exchange, GatewayFilterChain chain, AuthStatusResponse authStatus) {

    ServerHttpRequest.Builder requestBuilder =
        exchange
            .getRequest()
            .mutate()
            .header("X-Gateway-Token", internalProperties.getApiKey()) // 게이트웨이 인증 토큰 추가
            .header("X-User-Id", authStatus.getMemberId()) // memberId 매핑
            .header("X-User-Role", authStatus.getRole()); // role 매핑

    if (authStatus.getSellerId() != null) { // sellerId 매핑
      requestBuilder.header("X-Seller-Id", String.valueOf(authStatus.getSellerId()));
    }

    return chain.filter(exchange.mutate().request(requestBuilder.build()).build());
  }

  private Mono<Void> addInternalUserHeaders(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest mutatedRequest =
        exchange
            .getRequest()
            .mutate()
            .header("X-User-Id", "SYSTEM")
            .header("X-User-Role", "ROLE_SYSTEM")
            .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    ApiResponse<Void> errorResponse =
        new ApiResponse<>(
            false,
            ErrorStatus.AUTH_INVALID_TOKEN.getCode(),
            ErrorStatus.AUTH_INVALID_TOKEN.getMessage(),
            null); // ApiResponse 생성자 규격에 맞춤

    try {
      byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
      DataBuffer buffer = response.bufferFactory().wrap(bytes);
      return response.writeWith(Mono.just(buffer));
    } catch (JsonProcessingException e) {
      log.error("JSON 직렬화 에러", e);
      return response.setComplete();
    }
  }

  /**
   * Gateway의 라우팅 필터가 실행되어 하위 서비스로 트래픽이 넘어가기 전에 인증 및 헤더 조작을 완료해야 하므로 우선순위를 -1로 설정하여 타 필터들보다 먼저 실행되도록
   * 보장합니다.
   */
  @Override
  public int getOrder() {
    return -1;
  }
}
