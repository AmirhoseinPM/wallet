package com.example.wallet.security;

import com.example.wallet.domain.entity.Account;
import com.example.wallet.domain.entity.AccountDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityAuditorAware implements AuditorAware<Account> {
    @Override
    public Optional<Account> getCurrentAuditor() {
        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();
        Account account = ((AccountDetails) authentication.getPrincipal()).getAccount();
        return Optional.of(account);
    }
}
