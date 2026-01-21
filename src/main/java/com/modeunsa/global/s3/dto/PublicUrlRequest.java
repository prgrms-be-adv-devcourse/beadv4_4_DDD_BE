package com.modeunsa.global.s3.dto;

public record PublicUrlRequest(
    String rawKey, DomainType domainType, Long domainId, String filename, String contentType) {}
