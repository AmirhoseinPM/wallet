package com.example.wallet.transaction;

import com.example.wallet.exception.ErrorResponseService;
import com.example.wallet.security.TransactionOwnership;
import com.example.wallet.security.WalletOwnership;
import jakarta.validation.Valid;
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

    @PostMapping(value = "/wallet/{walletId}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @WalletOwnership
    public ResponseEntity<Object> createTransactionForWallet(
            @PathVariable Long walletId,
            @RequestBody @Valid
            TransactionDto transactionDto,
            BindingResult result) {

        transactionDto.setWalletId(walletId);

        var transactionResult = transactionService.create(transactionDto, result);

        if (result.hasErrors()) {
            return errorResponseService.returnValidationError(result);
        }

        return transactionResult;
    }

    @WalletOwnership
    @GetMapping(value = "/wallet/{id}" ,produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, List<Transaction>> getByWalletId(@PathVariable long id) {
        return transactionService.getByWalletId(id);
    }

    @GetMapping(value = "/{id}" ,produces = MediaType.APPLICATION_JSON_VALUE)
    @TransactionOwnership
    public Transaction getById(@PathVariable long id) {
        return transactionService.getById(id);
    }

}
