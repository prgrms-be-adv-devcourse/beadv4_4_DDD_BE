package com.modeunsa.shared.file.dto;

import com.modeunsa.boundedcontext.file.domain.DomainType;

public record UploadPathInfo(
    String profile, DomainType domainType, String filename, String uuid, String extension) {}
