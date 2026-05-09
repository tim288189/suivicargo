package com.elior.suivicargo.dtos;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<FieldErrorDetail> fieldErrors
) {

    public record FieldErrorDetail(String field, String message, Object rejectedValue) {}

    public static ApiError of(int status, String error, String code, String message, String path) {
        return new ApiError(Instant.now(), status, error, code, message, path, null);
    }

    public static ApiError ofValidation(String message, String path, List<FieldErrorDetail> fieldErrors) {
        return new ApiError(Instant.now(), 400, "Bad Request", "VALIDATION_ERROR", message, path, fieldErrors);
    }
}
