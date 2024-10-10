package com.example.wallet.transaction;

import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import com.example.wallet.wallet.Wallet;
import com.example.wallet.wallet.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final long MINIMUM_TRANSACTION_AMOUNT = 1L;
    private final long MAXIMUM_DAILY_WITHDRAWAL = 15_000_000L;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
    }

    public Map<String, List<Transaction>> getAll() {
        List<Transaction> transactions =
                transactionRepository.findByWallet_Account_NationalId(
                        getNationalIdFromSecurityContext()
                );

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toString().substring(0, 10)
                ));
    }

    public Map<String, List<Transaction>> getByWalletId(long walletId) {
        validateWalletOwnership(walletId);

        List<Transaction> transactions =
                transactionRepository.findByWallet_Id(walletId);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toString().substring(0, 10)
                ));
    }


    @Transactional
    public Transaction create(Transaction transaction) {

        // transaction amount must greater than 0
        if  (transaction.getAmount() < MINIMUM_TRANSACTION_AMOUNT)
            throw new ValidationException("Minimum Transaction's amount is " +
                    MINIMUM_TRANSACTION_AMOUNT);

        // validate wallet: existence and ownership and update it to transaction
        Wallet validatedWallet = validateWalletOwnership(transaction.getWallet().getId());
        transaction.setWallet(validatedWallet);

        // withdraw validation
        if (transaction.isWithdrawal()) {
            // check available balance is enough
            long MINIMUM_BALANCE = 15_000;
            if ((transaction.getAmount() > (transaction.getWallet().getBalance() - MINIMUM_BALANCE)))
                throw new ValidationException("Available balance is not enough for the withdrawal's amount");

            // maximum daily withdrawal validation
            validateTotalAcceptedWithdrawal(transaction);
        }

        // update wallet balance
        if (transaction.isWithdrawal())
            transaction.getWallet().setBalance(
                    transaction.getWallet().getBalance() - transaction.getAmount()
            );
        else
            transaction.getWallet().setBalance(
                    transaction.getWallet().getBalance() + transaction.getAmount()
            );

        // save transaction and related wallet
        try {
            transaction.setCreatedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);
        } catch (Exception ex) {
            throw new ValidationException("SQL exception in saving transaction");
        }
        return transaction;
    }

    private Wallet validateWalletOwnership(long walletId) {
        // get wallet
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Wallet not found"));

        // check ownership
        if (!wallet.getAccount().getNationalId().equals(getNationalIdFromSecurityContext()))
            throw new AccessDeniedException("Access denied !!!");

        return wallet;
    }

    private void validateTotalAcceptedWithdrawal(Transaction transaction) {
        // check today's withdraw plus current amount, is not more than 15_000_000
        LocalDateTime currentDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayWithdraw;

        try {
            todayWithdraw = transactionRepository.calculateTodayWithdraw(
                    currentDay, transaction.getWallet().getId())
                    .orElse(0L);
        } catch(Exception e) {
            throw new ValidationException(e.getMessage() + "\n" + e.getCause());
        }
        if ((todayWithdraw + transaction.getAmount()) > MAXIMUM_DAILY_WITHDRAWAL)
            throw new ValidationException( transaction.getAmount() +
                    " is not acceptable. Maximum daily withdrawal is: " +
                    MAXIMUM_DAILY_WITHDRAWAL);
    }

    private String getNationalIdFromSecurityContext() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

}
