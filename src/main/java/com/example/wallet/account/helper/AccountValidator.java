package com.example.wallet.account.helper;

import com.example.wallet.account.Account;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.time.*;


@Component
public class AccountValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Account.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        Account account = (Account) target;

        Long age = getAge(account);

        if ((age == null) || (
                (account.getGender() != null) &&
                (account.getGender().equals("m")) &&
                (age >= 18) &&
                (account.getMilitaryStatus() == null))
        ) {
            errors.reject("militaryStatus", "militaryStatus is required for MALEs older than 18");
        }
    }

    private static Long getAge(Account account) {
        Long age = null;
        if (account.getBirthDate() != null) {
            Duration duration = Duration.between(LocalDateTime.of(
                    account.getBirthDate(), LocalTime.now()), ZonedDateTime.ofInstant(Instant.now(), ZoneId.of("Asia/Tehran")));
            age = Math.abs(duration.toDays() / 365);
        }
        return age;
    }
}
