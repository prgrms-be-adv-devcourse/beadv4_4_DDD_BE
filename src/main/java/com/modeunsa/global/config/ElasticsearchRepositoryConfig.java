package com.modeunsa.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@Profile("!test")
@EnableElasticsearchRepositories(basePackages = "com.modeunsa.boundedcontext.product.out.search")
public class ElasticsearchRepositoryConfig {}
