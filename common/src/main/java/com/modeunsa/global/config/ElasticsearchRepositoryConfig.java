package com.modeunsa.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.data.elasticsearch.autoconfigure.DataElasticsearchAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(name = "app.elasticsearch.enabled", havingValue = "true")
@Import(DataElasticsearchAutoConfiguration.class)
@EnableElasticsearchRepositories(basePackages = "com.modeunsa.boundedcontext.product.out.search")
public class ElasticsearchRepositoryConfig {}
