package com.modeunsa.boundedcontext.product.elasticsearch.infra;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import java.net.URI;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

  @Value("${spring.elasticsearch.uris:http://localhost:9200}")
  private String elasticsearchUri;

  @Bean
  public RestClient restClient() {
    URI uri = URI.create(elasticsearchUri.split(",")[0].trim());
    return RestClient.builder(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())).build();
  }

  @Bean
  public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
    return new RestClientTransport(restClient, new JacksonJsonpMapper());
  }

  @Bean
  public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
    return new ElasticsearchClient(transport);
  }
}
