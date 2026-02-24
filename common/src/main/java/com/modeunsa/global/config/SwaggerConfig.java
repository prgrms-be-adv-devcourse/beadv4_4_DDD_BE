package com.modeunsa.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    // 1. JWT 토큰 (Bearer) 설정
    SecurityScheme bearerAuth =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

    // 2. 내부 API 키 설정
    SecurityScheme internalApiKey =
        new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name("X-INTERNAL-API-KEY");

    // 3. API 기본 정보
    Info info =
        new Info()
            .title("Modeunsa API")
            .version("v1.0.0")
            .description("프로그래머스 단기심화4 DDD 세미프로젝트 스웨거 API 문서입니다.");

    return new OpenAPI()
        .addServersItem(new Server().url("/"))
        .info(info)
        // 두 가지 인증 방식을 모두 API 레벨에 적용
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .addSecurityItem(new SecurityRequirement().addList("internalApiKey"))
        .components(
            new Components()
                .addSecuritySchemes("bearerAuth", bearerAuth)
                .addSecuritySchemes("internalApiKey", internalApiKey));
  }
}