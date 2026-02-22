package com.modeunsa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SwaggerProperties.class)
@RequiredArgsConstructor
public class SwaggerConfig {

  private final SwaggerProperties swaggerProperties;

  @Bean
  public OpenAPI openAPI() {
    Server server = new Server();
    server.setUrl(swaggerProperties.serverUrl());
    server.setDescription(swaggerProperties.description());

    // SecurityScheme 설정
    SecurityScheme bearerAuth =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

    SecurityScheme internalApiKey =
        new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name("X-INTERNAL-API-KEY");

    Info info =
        new Info()
            .title("Modeunsa API")
            .version("v1.0.0")
            .description("프로그래머스 단기심화4 DDD 세미프로젝트 스웨거 API 문서입니다.");

    return new OpenAPI()
        .servers(List.of(server)) // 서버 목록에 현재 서버만 등록
        .info(info)
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .addSecurityItem(new SecurityRequirement().addList("internalApiKey"))
        .components(
            new Components()
                .addSecuritySchemes("bearerAuth", bearerAuth)
                .addSecuritySchemes("internalApiKey", internalApiKey));
  }
}
