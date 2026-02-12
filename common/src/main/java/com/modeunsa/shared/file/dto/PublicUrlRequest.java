package com.modeunsa.shared.file.dto;

import com.modeunsa.shared.file.DomainType;

public record PublicUrlRequest(String rawKey, DomainType domainType, String contentType) {}
