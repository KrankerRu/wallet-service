package com.kranker.walletservice.exception;

import com.kranker.walletservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(WalletServiceException.class)
  public ResponseEntity<ErrorResponse> handleWalletServiceException(
      WalletServiceException ex,
      HttpServletRequest request) {
    log.warn("Wallet service error: {} - {}", ex.getErrorCode(), ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .status(ex.getHttpStatus().value())
        .message(ex.getMessage())
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return new ResponseEntity<>(error, ex.getHttpStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    log.warn("Validation error: {}", ex.getMessage());

    String errorMessage = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .findFirst()
        .orElse("Validation error");

    ErrorResponse error = ErrorResponse.builder()
        .status(ErrorCode.INVALID_AMOUNT.getHttpStatus().value())
        .message(errorMessage)
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleInvalidJson(
      HttpMessageNotReadableException ex,
      HttpServletRequest request) {
    log.warn("Invalid JSON: {}", ex.getMessage());

    ErrorResponse error = ErrorResponse.builder()
        .status(ErrorCode.INVALID_OPERATION_TYPE.getHttpStatus().value())
        .message("Invalid JSON format")
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(
      Exception ex,
      HttpServletRequest request) {
    log.error("Unexpected error: ", ex);

    ErrorResponse error = ErrorResponse.builder()
        .status(500)
        .message("Internal server error")
        .path(request.getRequestURI())
        .timestamp(LocalDateTime.now())
        .build();

    return ResponseEntity.internalServerError().body(error);
  }
}