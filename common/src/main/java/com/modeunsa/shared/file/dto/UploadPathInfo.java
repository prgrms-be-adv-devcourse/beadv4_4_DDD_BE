package com.modeunsa.shared.file.dto;

import com.modeunsa.shared.file.DomainType;

public record UploadPathInfo(
    String profile, DomainType domainType, String filename, String uuid, String extension) {}
