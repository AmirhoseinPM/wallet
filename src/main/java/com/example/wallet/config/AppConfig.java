package com.example.wallet.config;

import com.example.wallet.account.AccountDetailsRepository;
import com.example.wallet.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@ComponentScan(basePackages = "com.example.wallet")
public class AppConfig implements WebMvcConfigurer {
    private final AccountDetailsRepository accountDetailsRepository;

    public AppConfig(AccountDetailsRepository accountDetailsRepository) {
        this.accountDetailsRepository = accountDetailsRepository;
    }

    @Bean
    UserDetailsService userDetailsService() {
        // how to retrieve the user using the UserRepository that is injected
        return nationalID -> accountDetailsRepository.findByAccount_NationalId(nationalID)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        // creates an instance of the BCryptPasswordEncoder
        // used to encode the plain user password.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // sets the new strategy to perform the authentication.
        return config.getAuthenticationManager();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

}
