package com.example.wallet.transaction;

import com.example.wallet.exception.ErrorResponse;
import com.example.wallet.exception.ErrorResponseService;
import com.example.wallet.exception.ValidationException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    private final TransactionService transactionService;
    private final ErrorResponseService errorResponseService;

    private final Logger log = LoggerFactory.getLogger(TransactionController.class);
    @Autowired
    public TransactionController(TransactionService transactionService,
                                 ErrorResponseService errorResponseService) {
        this.transactionService = transactionService;
        this.errorResponseService = errorResponseService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Transaction>> getAll() {
        return transactionService.getAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> create(
            @RequestBody @Valid
            TransactionDto transactionDto,
            BindingResult result) {

        if (result.hasErrors()) {
            return errorResponseService.returnValidationError(result);
        }
        return ResponseEntity.ok(transactionService.create(transactionDto));
    }

    @GetMapping(value = "/wallet/{id}" ,produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Transaction>> getByWalletId(@PathVariable long id) {
        return transactionService.getByWalletId(id);
    }

    @GetMapping(value = "/{id}" ,produces = MediaType.APPLICATION_JSON_VALUE)
    public Transaction getById(@PathVariable long id) {
        return transactionService.getById(id);
    }
}
