package com.modeunsa.shared.file.dto;

import com.modeunsa.shared.file.DomainType;

public record PresignedUrlRequest(DomainType domainType, String ext, String contentType) {}
