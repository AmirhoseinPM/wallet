package com.example.wallet.transaction;

import com.example.wallet.exception.ErrorResponseService;
import com.example.wallet.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.LocalDateTime;

@Component
public class WithdrawalValidator implements Validator {

    private final TransactionRepository transactionRepository;
    private final ErrorResponseService errorResponseService;

    @Autowired
    public WithdrawalValidator(
            TransactionRepository transactionRepository,
            ErrorResponseService errorResponseService) {
        this.transactionRepository = transactionRepository;
        this.errorResponseService = errorResponseService;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Transaction.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Transaction transaction = (Transaction) target;

        // check available balance is enough
        long MINIMUM_BALANCE = 15_000;
        if ((transaction.getAmount() > (transaction.getWallet().getBalance() - MINIMUM_BALANCE))) {
            errors.reject("400", "Available balance is not enough for the withdrawal's amount");
            return;
        }
        // check today's withdraw plus current amount, is not more than 15_000_000
        LocalDateTime currentDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        long todayWithdraw;

        try {
            todayWithdraw = transactionRepository.calculateTodayWithdraw(
                            currentDay, transaction.getWallet().getId())
                    .orElseThrow(
                            () -> new ValidationException("Something went wrong")
                    );
        } catch(Exception e) {
            throw new ValidationException(e.getMessage() + "\n" + e.getCause());
        }
        long MAXIMUM_DAILY_WITHDRAWAL = 15_000_000L;
        if ((todayWithdraw + transaction.getAmount()) > MAXIMUM_DAILY_WITHDRAWAL)
            errors.reject("400",  transaction.getAmount() +
                    " is not acceptable. Maximum daily withdrawal is: " +
                    MAXIMUM_DAILY_WITHDRAWAL);

    }
}
