package com.example.wallet.service.spec;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;

public interface ErrorResponseServiceSpec {
    ResponseEntity<Object> returnValidationError(Errors errors);
}
