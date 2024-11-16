package com.example.wallet.service.spec;

import com.example.wallet.domain.entity.Transaction;
import com.example.wallet.dto.TransactionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

public interface TransactionServiceSpec {
    Map<String, List<Transaction>> getAll(Integer pageNumber);
    Transaction getById(long id);
    Map<String, List<Transaction>> getByWalletId(long walletId, Integer pageNumber);
    ResponseEntity<Object> create(TransactionDto transactionDto, BindingResult result);
}
