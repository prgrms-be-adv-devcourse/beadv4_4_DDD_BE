package com.modeunsa.global.s3.dto;

public record UploadPathInfo(
    String profile, DomainType domainType, String filename, String uuid, String extension) {}
