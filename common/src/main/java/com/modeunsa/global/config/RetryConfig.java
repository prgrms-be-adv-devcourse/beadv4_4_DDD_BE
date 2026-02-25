package com.modeunsa.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;

@EnableResilientMethods
@Configuration
public class RetryConfig {}
