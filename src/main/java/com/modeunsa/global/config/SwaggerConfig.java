package com.modeunsa.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    Info info =
        new Info()
            .title("Modeunsa API")
            .version("v1.0.0")
            .description("프로그래머스 단기심화4 DDD 세미프로젝트 스웨거 API 문서입니다.");

    // SecurityScheme 설정
    SecurityScheme bearerAuth =
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .in(SecurityScheme.In.HEADER)
            .name("Authorization");

    // SecurityRequirement 설정
    SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

    return new OpenAPI()
        .info(info)
        .addSecurityItem(securityRequirement)
        .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth));
  }
}
