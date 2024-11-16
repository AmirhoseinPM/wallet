package com.example.wallet.service.impl;

import com.example.wallet.exception.ErrorResponse;
import com.example.wallet.service.spec.ErrorResponseServiceSpec;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import java.util.List;

@Service
public class ErrorResponseService implements ErrorResponseServiceSpec {
    public ResponseEntity<Object> returnValidationError(Errors errors) {
        List<String> errorsList = errors.getAllErrors()
                .stream()
                .map(e -> (e.getDefaultMessage() == null ? "": e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                new ErrorResponse("400", "bad request", errorsList));
    }
}
