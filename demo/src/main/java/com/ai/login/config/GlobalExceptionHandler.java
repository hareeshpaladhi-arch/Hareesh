package com.ai.login.config;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import com.ai.login.DTO.ApiResponse;

import org.springframework.http.*;



@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        StringBuilder errorMsg = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMsg.append(error.getField())
                    .append(" : ")
                    .append(error.getDefaultMessage())
                    .append(" ");
        });

        ApiResponse response = new ApiResponse(errorMsg.toString(), false);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
