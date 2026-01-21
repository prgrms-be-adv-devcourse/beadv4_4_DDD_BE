package com.modeunsa.shared.file.dto;

public record PresignedUrlRequest(
    Long domainId, DomainType domainType, String contentType, String filename) {}
