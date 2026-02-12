package com.modeunsa.shared.file.dto;

import com.modeunsa.boundedcontext.file.domain.DomainType;

public record PublicUrlRequest(String rawKey, DomainType domainType, String contentType) {}
