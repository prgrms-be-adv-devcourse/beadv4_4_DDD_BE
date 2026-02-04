package com.modeunsa.global.s3.dto;

public record PublicUrlRequest(String rawKey, DomainType domainType, String contentType) {}
