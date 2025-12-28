package com.relyon.metasmart.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, String> errors;

    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = null;
    }

    public ErrorResponse(String message, Map<String, String> errors) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }
}
