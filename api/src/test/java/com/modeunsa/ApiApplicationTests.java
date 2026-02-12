package com.modeunsa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class ApiApplicationTests {

  @MockitoBean(name = "orderDataInitApplicationRunner")
  private ApplicationRunner runner;

  @Test
  void contextLoads() {}
}
