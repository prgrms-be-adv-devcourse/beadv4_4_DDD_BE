package com.modeunsa.global.s3.dto;

public record UploadPathInfo(
    String profile,
    DomainType domainType,
    Long domainId,
    String filename,
    String uuid,
    String extension) {}
