package com.modeunsa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class ModeunsaApplicationTests {

  @MockitoBean(name = "orderDataInitApplicationRunner")
  private ApplicationRunner runner;

  @MockitoBean private co.elastic.clients.elasticsearch.ElasticsearchClient elasticsearchClient;

  @MockitoBean
  private org.springframework.data.elasticsearch.core.ElasticsearchOperations
      elasticsearchOperations;

  @Test
  void contextLoads() {}
}
