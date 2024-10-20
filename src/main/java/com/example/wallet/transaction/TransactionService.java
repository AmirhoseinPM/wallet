package com.example.wallet.transaction;

import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import com.example.wallet.wallet.Wallet;
import com.example.wallet.wallet.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WithdrawalValidator withdrawalValidator;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              WithdrawalValidator withdrawalValidator) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.withdrawalValidator = withdrawalValidator;
    }

    public Map<String, List<Transaction>> getAll() {
        List<Transaction> transactions =
                transactionRepository.findByWallet_Account_NationalIdAndWallet_IsActive (
                        getNationalIdFromSecurityContext(), true
                );

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toString().substring(0, 10)
                ));
    }
    public Transaction getById(long id) {

        return transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction not found")
        );
    }
    public Map<String, List<Transaction>> getByWalletId(long walletId) {

        List<Transaction> transactions =
                transactionRepository.findByWallet_Id(walletId);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toString().substring(0, 10)
                ));
    }


    @Transactional
    public ResponseEntity<Object> create(TransactionDto transactionDto, BindingResult result) {

        Transaction transaction = mapTransactionDtoToEntity(transactionDto);

        // Retrieve wallet and set it to transaction
        Wallet wallet = walletRepository.findById(transactionDto.getWalletId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Wallet not found"));
        transaction.setWallet(wallet);


        // update wallet balance
        if (transaction.isWithdrawal()) {
            // withdraw validation
            withdrawalValidator.validate(transaction, result);
            // check withdrawal validation
            if (result.hasErrors())
                return null;

            transaction.getWallet().setBalance(
                    transaction.getWallet().getBalance() - transaction.getAmount()
            );
        }
        else {
            transaction.getWallet().setBalance(
                    transaction.getWallet().getBalance() + transaction.getAmount()
            );
        }

        // save transaction and related wallet
        try {
            transaction.setCreatedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);
        } catch (Exception ex) {
            throw new ValidationException("SQL exception in saving transaction");
        }

        return ResponseEntity.ok(transaction);
    }


    private String getNationalIdFromSecurityContext() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private Transaction mapTransactionDtoToEntity(TransactionDto transactionDto) {

        Transaction transaction = new Transaction();
        transaction.setWithdrawal(transactionDto.isWithdrawal());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDescription(transactionDto.getDescription());

        return transaction;
    }

}
