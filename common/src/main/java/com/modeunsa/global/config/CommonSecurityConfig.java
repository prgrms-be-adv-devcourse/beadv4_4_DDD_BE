package com.modeunsa.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
public class CommonSecurityConfig {

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.withDefaultRolePrefix()
        .role("SYSTEM")
        .implies("ADMIN") // SYSTEM은 ADMIN의 모든 권한을 가짐
        .role("HOLDER")
        .implies("ADMIN") // HOLDER는 ADMIN의 모든 권한을 가짐
        .role("ADMIN")
        .implies("SELLER") // ADMIN은 SELLER의 모든 권한을 가짐
        .role("SELLER")
        .implies("MEMBER") // SELLER는 MEMBER의 모든 권한을 가짐
        .build();
  }
}
