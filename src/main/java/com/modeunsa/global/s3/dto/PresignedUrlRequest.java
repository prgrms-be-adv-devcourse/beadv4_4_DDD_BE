package com.modeunsa.global.s3.dto;

public record PresignedUrlRequest(
    Long domainId, DomainType domainType, String ext, String contentType) {}
