package com.example.ccmps.exception;
import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(

        int status,
        String code,
        String message,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors

) {
}