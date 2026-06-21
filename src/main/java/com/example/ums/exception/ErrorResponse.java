package com.example.ums.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    private int status;
    private String error;
    private String message;
    private String path;

    // List of failed validation(400s from @Valid).
    private List<FieldError> fieldErrors;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }
}

