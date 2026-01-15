package com.modeunsa.boundedcontext.payment.in;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeunsa.global.exception.ExceptionAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@ExtendWith(MockitoExtension.class)
public abstract class BasePaymentControllerTest {

  protected MockMvc mockMvc;
  protected ObjectMapper objectMapper;

  @BeforeEach
  void setUpBase() {
    objectMapper = new ObjectMapper();
  }

  protected void setUpMockMvc(Object controller) {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();
    mockMvc =
        MockMvcBuilders.standaloneSetup(controller)
            .setValidator(validator)
            .setControllerAdvice(new ExceptionAdvice())
            .build();
  }
}
