package com.example.wallet.transaction;

import com.example.wallet.account.AccountService;
import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import com.example.wallet.wallet.Wallet;
import com.example.wallet.wallet.WalletRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private final AccountService accountService;
    private final long MINIMUM_TRANSACTION_AMOUNT = 1L;
    private final long MAXIMUM_DAILY_WITHDRAWAL = 15_000_000L;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.accountService = accountService;
    }

    public Map<String, List<Transaction>> getAll() {
        // check account activation
        if(!accountService.
                findByNationalId(getNationalIdFromSecurityContext())
                .isActive())
            throw new ResourceNotFoundException("Account not found");

        List<Transaction> transactions =
                transactionRepository.findByWallet_Account_NationalId(
                        getNationalIdFromSecurityContext()
                ).stream()
                        .filter(transaction -> transaction.getWallet().isActive())
                        .toList();

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCreatedAt().toString().substring(0, 10)
                ));
    }
    public Transaction getById(long id) {
        if(!accountService.
                findByNationalId(getNationalIdFromSecurityContext())
                .isActive())
            throw new ResourceNotFoundException("Account not found");

        Transaction transaction = transactionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Transaction not found")
        );

        if (!transaction.getWallet().isActive())
            throw new ResourceNotFoundException("Transaction not exists");

        // check ownership
        if (
                (transaction.getWallet() == null) ||
                        (transaction.getWallet().getAccount() == null) ||
                        (!transaction.getWallet().getAccount().getNationalId()
                                .equals(getNationalIdFromSecurityContext()))
        )
            throw new AccessDeniedException("Access denied!");

        return transaction;
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
    public Transaction create(TransactionDto transactionDto) {

        Transaction transaction = mapTransactionDtoToEntity(transactionDto);

        // transaction amount must greater than 0
        if  (transaction.getAmount() < MINIMUM_TRANSACTION_AMOUNT)
            throw new ValidationException(
                    "Minimum Transaction's amount is " + MINIMUM_TRANSACTION_AMOUNT);

        // validate wallet: existence and ownership and update it to transaction
        Wallet validatedWallet = validateWalletOwnership(transactionDto.getWalletId());
        transaction.setWallet(validatedWallet);


        // update wallet balance
        if (transaction.isWithdrawal()) {
            // withdraw validation
            validateWithdrawalTransaction(transaction);
            transaction.getWallet().setBalance(
                    transaction.getWallet().getBalance() - transaction.getAmount()
            );
        }
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

    private void validateWithdrawalTransaction(Transaction transaction) {
        // check available balance is enough
        long MINIMUM_BALANCE = 15_000;
        if ((transaction.getAmount() > (transaction.getWallet().getBalance() - MINIMUM_BALANCE)))
            throw new ValidationException("Available balance is not enough for the withdrawal's amount");

        // maximum daily withdrawal validation
        validateTotalAcceptedWithdrawal(transaction);
    }

    private Transaction mapTransactionDtoToEntity(TransactionDto transactionDto) {

        Transaction transaction = new Transaction();
        transaction.setWithdrawal(transactionDto.isWithdrawal());
        transaction.setAmount(transactionDto.getAmount());
        transaction.setDescription(transactionDto.getDescription());

        return transaction;
    }

    private Wallet validateWalletOwnership(long walletId) {
        // get wallet
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Wallet not found"));

        if (
                (!wallet.getAccount().isActive()) ||
                (!wallet.isActive())
        )
            throw new ResourceNotFoundException("This wallet does not exists");

        // check ownership
        if ((wallet.getAccount() == null) || (wallet.getAccount().getNationalId() == null) ||
                (!wallet.getAccount().getNationalId().equals(getNationalIdFromSecurityContext())))
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
