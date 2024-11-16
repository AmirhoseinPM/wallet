package com.example.wallet.service.impl;

import com.example.wallet.domain.entity.Account;
import com.example.wallet.domain.entity.AccountDetails;
import com.example.wallet.domain.entity.Transaction;
import com.example.wallet.dto.TransactionDto;
import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import com.example.wallet.repository.TransactionPagingAndSortingRepo;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.domain.entity.Wallet;
import com.example.wallet.repository.WalletRepository;

import com.example.wallet.service.spec.TransactionServiceSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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
public class TransactionService implements TransactionServiceSpec {

    public static final int PAGE_SIZE = 10;
    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final TransactionPagingAndSortingRepo transactionPagingAndSortingRepo;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              WalletRepository walletRepository,
                              TransactionPagingAndSortingRepo transactionPagingAndSortingRepo) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.transactionPagingAndSortingRepo = transactionPagingAndSortingRepo;
    }

    public Map<String, List<Transaction>> getAll(Integer pageNumber) {
        PageRequest page = getPageRequest(pageNumber);

        List<Transaction> transactions =
                transactionPagingAndSortingRepo.findByWallet_Account_NationalIdAndWallet_IsActive (
                        getNationalIdFromSecurityContext(),
                        page
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
    public Map<String, List<Transaction>> getByWalletId(long walletId, Integer pageNumber) {
        PageRequest page = getPageRequest(pageNumber);

        List<Transaction> transactions =
                transactionPagingAndSortingRepo.findByWallet_Id(
                        walletId,
                        page
                );

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
            // check available balance is enough
            long MINIMUM_BALANCE = 15_000;
            if ((transaction.getAmount() > (transaction.getWallet().getBalance() - MINIMUM_BALANCE))) {
                result.reject("400", "Available balance is not enough for the withdrawal's amount");
                return null;
            }
            // check today's withdraw plus current amount, is not more than 15_000_000
            LocalDateTime currentDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            long todayWithdraw;
            try {
                // retrieve total today's Withdrawal from db
                todayWithdraw = transactionRepository.calculateTodayWithdraw(
                                currentDay, transaction.getWallet().getId())
                        .orElseThrow(
                                () -> new ValidationException("Something went wrong")
                        );
            } catch(Exception e) {
                throw new ValidationException(e.getMessage() + "\n" + e.getCause());
            }
            long MAXIMUM_DAILY_WITHDRAWAL = 15_000_000L;
            if ((todayWithdraw + transaction.getAmount()) > MAXIMUM_DAILY_WITHDRAWAL) {
                result.reject("400", transaction.getAmount() +
                        " is not acceptable. Maximum daily withdrawal is: " +
                        MAXIMUM_DAILY_WITHDRAWAL);
                return null;
            }
            // update wallet balance based on transaction amount
            transaction.getWallet().setBalance(
                    transaction.getWallet().getBalance() - transaction.getAmount()
            );
        }
        else {
            // update wallet balance based on transaction amount
            transaction.getWallet().setBalance(
                    transaction.getWallet().getBalance() + transaction.getAmount()
            );
        }
        Account createdBy = ((AccountDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal()).getAccount();
        // save transaction and related wallet
        transaction.setCreatedBy(createdBy);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction = transactionRepository.save(transaction);


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

    private static Integer convertPageNumber(Integer pageNumber) {
        if (pageNumber == null)
            pageNumber = 0;
        else if (pageNumber < 1)
            throw new ValidationException("pageNumber must be grater than 0");
        else
            pageNumber--;
        return pageNumber;
    }

    private static PageRequest getPageRequest(Integer pageNumber) {
        // pageNumber in Pageable starts from 0 but user request start from 1
        pageNumber = convertPageNumber(pageNumber);
        return PageRequest.of(pageNumber, PAGE_SIZE);
    }

}
