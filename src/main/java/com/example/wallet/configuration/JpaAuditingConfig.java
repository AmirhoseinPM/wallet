package com.example.wallet.configuration;


import com.example.wallet.domain.entity.Account;
import com.example.wallet.security.SecurityAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<Account> auditorProvider() {
        return new SecurityAuditorAware();
    }

}
