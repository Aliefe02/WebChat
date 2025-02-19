package com.webchat.server.controller;

import com.webchat.server.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class CustomErrorController {

    @ExceptionHandler(UnauthorizedException.class)
    ResponseEntity handleUnauthorizedError(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(TransactionSystemException.class)
    ResponseEntity handleJPAViolations(TransactionSystemException exception){
        ResponseEntity.BodyBuilder responseEntity = ResponseEntity.badRequest();
        if (exception.getCause() instanceof ConstraintViolationException){
            ConstraintViolationException ve = (ConstraintViolationException) exception.getCause().getCause();
            List errors = ve.getConstraintViolations().stream()
                    .map(constraintViolation -> {
                        Map<String, String> errMap = new HashMap<>();
                        errMap.put(String.valueOf(constraintViolation.getPropertyPath()), constraintViolation.getMessage());
                        return errMap;
                    }).collect(Collectors.toList());
            return responseEntity.body(errors);
        }
        return responseEntity.build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity handleBindErrors(MethodArgumentNotValidException exception){

        List errorList = exception.getFieldErrors().stream()
                .map(fieldError -> {
                    Map<String, String> errorMap = new HashMap<>();
                    errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
                    return errorMap;
                }).collect(Collectors.toList());

        return ResponseEntity.badRequest().body(errorList);

    }
}

