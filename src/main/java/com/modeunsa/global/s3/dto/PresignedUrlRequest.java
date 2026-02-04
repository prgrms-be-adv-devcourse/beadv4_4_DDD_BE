package com.modeunsa.global.s3.dto;

public record PresignedUrlRequest(DomainType domainType, String ext, String contentType) {}
