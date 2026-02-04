package com.modeunsa.global.file.dto;

import com.modeunsa.boundedcontext.file.domain.DomainType;

public record PresignedUrlRequest(DomainType domainType, String ext, String contentType) {}
