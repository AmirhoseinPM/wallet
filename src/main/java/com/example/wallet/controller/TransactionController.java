package com.example.wallet.controller;

import com.example.wallet.domain.entity.Transaction;
import com.example.wallet.security.aop.TransactionOwnership;
import com.example.wallet.security.aop.WalletOwnership;
import com.example.wallet.dto.TransactionDto;
import com.example.wallet.service.spec.ErrorResponseServiceSpec;
import com.example.wallet.service.spec.TransactionServiceSpec;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    private final TransactionServiceSpec transactionService;
    private final ErrorResponseServiceSpec errorResponseService;

    @Autowired
    public TransactionController(TransactionServiceSpec transactionService,
                                 ErrorResponseServiceSpec errorResponseService) {
        this.transactionService = transactionService;
        this.errorResponseService = errorResponseService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Transaction>> getAll(
            @RequestParam(required = false)
            Integer pageNumber) {
        return transactionService.getAll(pageNumber);
    }

    @PostMapping(value = "/wallet/{walletId}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @WalletOwnership
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> createTransactionForWallet(
            @PathVariable Long walletId,
            @RequestBody @Valid
            TransactionDto transactionDto,
            BindingResult result) {

        // check annotation validation of TransactionDto
        if (result.hasErrors()) {
            return errorResponseService.returnValidationError(result);
        }
        // try to create transaction
        transactionDto.setWalletId(walletId);
        var transactionResult = transactionService.create(transactionDto, result);

        // check validation of Withdrawal validator
        if (result.hasErrors()) {
            return errorResponseService.returnValidationError(result);
        }

        return transactionResult;
    }

    @WalletOwnership
    @GetMapping(value = "/wallet/{id}" ,produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Transaction>> getByWalletId(
            @PathVariable long id,
            @RequestParam(required = false)
            Integer pageNumber) {


        return transactionService.getByWalletId(id, pageNumber);
    }



    @GetMapping(value = "/{id}" ,produces = MediaType.APPLICATION_JSON_VALUE)
    @TransactionOwnership
    public Transaction getById(@PathVariable long id) {
        return transactionService.getById(id);
    }

}
