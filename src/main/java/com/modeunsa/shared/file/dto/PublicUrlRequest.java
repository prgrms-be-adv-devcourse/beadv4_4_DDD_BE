package com.modeunsa.shared.file.dto;

public record PublicUrlRequest(
    String rawKey, DomainType domainType, Long domainId, String filename, String contentType) {}
