package com.example.wallet.security.aop;

import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.domain.entity.Transaction;
import com.example.wallet.repository.TransactionRepository;
import com.example.wallet.domain.entity.Wallet;
import com.example.wallet.repository.WalletRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OwnershipAspect {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public OwnershipAspect(
            WalletRepository walletRepository,
            TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Around("@annotation(com.example.wallet.security.aop.WalletOwnership)")
    public Object checkWalletOwnerShip(ProceedingJoinPoint joinPoint) {

        // check wallet owned to account
        String nationalId;
        Long walletId;

        try {
            // extract walletId from controller method first argument
            walletId = (Long) joinPoint.getArgs()[0];
            // extract Account_NationalId from SecurityContext
            nationalId = SecurityContextHolder
                    .getContext().getAuthentication().getName();
        } catch (Exception e) {
            throw new AccessDeniedException("Access denied!!!");
        }
        // Retrieve wallet
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(
                () -> new ResourceNotFoundException("This wallet does not exists!")
        );
        // validate wallet's activation and ownership
        if (!wallet.isActive())
            throw new ResourceNotFoundException("Wallet not found");
        else if (!wallet.getAccount()
                .getNationalId().equals(nationalId))
            throw new AccessDeniedException("Access to this wallet is denied");

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    @Around("@annotation(com.example.wallet.security.aop.TransactionOwnership)")
    public Object checkTransactionOwnerShip(ProceedingJoinPoint joinPoint) {

        // check transaction owned to account
        String nationalId;
        Long transactionId;

        try {
            // extract transactionId from controller method first argument
            transactionId = (Long) joinPoint.getArgs()[0];
            // extract Account_NationalId from SecurityContext
            nationalId = SecurityContextHolder
                    .getContext().getAuthentication().getName();
        } catch (Exception e) {
            throw new AccessDeniedException("Access denied");
        }
        // Retrieve transaction
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(
                () -> new ResourceNotFoundException("This transaction does not exists!")
        );
        // validate wallet's activation and ownership
        if (!transaction.getWallet().isActive())
            throw new ResourceNotFoundException("Transaction not found");
        else if (!transaction.getWallet().getAccount()
                    .getNationalId().equals(nationalId))
            throw new AccessDeniedException("Access to this transaction is denied");

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


}
