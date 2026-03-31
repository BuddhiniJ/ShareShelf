package com.shareshelf.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // omit null fields from JSON
public class ErrorResponse {

    private int status;
    private String error;       // short error type e.g. "Not Found"
    private String message;     // human-readable detail
    private String path;        // which endpoint was hit
    private LocalDateTime timestamp;

    // Only populated for validation errors — field → message pairs
    private Map<String, String> validationErrors;
}