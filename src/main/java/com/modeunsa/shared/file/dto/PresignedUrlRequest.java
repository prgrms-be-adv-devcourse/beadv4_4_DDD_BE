package com.modeunsa.shared.file.dto;

public record PresignedUrlRequest(Long id, String group, String contentType, String filename) {}
