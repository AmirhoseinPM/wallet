package com.example.wallet.service;

import com.example.wallet.exception.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.List;

@Service
public class ErrorResponseService {
    public ResponseEntity<Object> returnValidationError(BindingResult result) {
        List<String> errors = result.getAllErrors()
                .stream()
                .map(e -> (e.getDefaultMessage() == null ? "": e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ErrorResponse("400", "bad request", errors));
    }
}
