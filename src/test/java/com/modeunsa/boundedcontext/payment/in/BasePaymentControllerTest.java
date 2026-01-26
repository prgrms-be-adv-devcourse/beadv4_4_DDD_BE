package com.modeunsa.boundedcontext.payment.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.boundedcontext.member.domain.types.MemberRole;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modeunsa.global.exception.ExceptionAdvice;
import com.modeunsa.global.security.CustomUserDetails;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
public abstract class BasePaymentControllerTest {

  protected MockMvc mockMvc;
  protected ObjectMapper objectMapper;

  @BeforeEach
  void setUpBase() {
    SecurityContextHolder.clearContext();
    objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  }

  protected void setUpMockMvc(Object controller) {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .setControllerAdvice(new ExceptionAdvice())
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
  }

  /** 테스트용 SecurityContext 설정 헬퍼 메서드 */
  protected void setSecurityContext(Long memberId, MemberRole role) {
    CustomUserDetails userDetails = new CustomUserDetails(memberId, role);
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
