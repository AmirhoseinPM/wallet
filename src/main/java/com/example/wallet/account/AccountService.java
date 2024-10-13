package com.example.wallet.account;

import com.example.wallet.jwt.JwtService;
import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountDetailsRepository accountDetailsRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final Logger log = LoggerFactory.getLogger(AccountService.class);


    @Autowired
    public AccountService(AccountRepository accountRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authenticationManager,
                          AccountDetailsRepository accountDetailsRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.accountDetailsRepository = accountDetailsRepository;
    }

    public Account signup(Account account) throws ValidationException {

        // encrypt account password before saving it
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setActive(true);
        AccountDetails accountDetails = new AccountDetails(account);
        // try to save account and return token and account info
        try {
            accountDetails = accountDetailsRepository.save(accountDetails);
            account = accountDetails.getAccount();
            log.info(accountDetails.toString());
        } catch (RuntimeException e) {
            throw new ValidationException(
                    "Account creation failed, national id or phone number or email already exist");
        }
        return account;
    }

    public TokenDto login(LoginDto input) {
        String jwtToken;

        try {
            // validate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getNationalId(),
                            input.getPassword()
                    ));
            // Retrieve accountDetails
            AccountDetails accountDetails =
                    accountDetailsRepository.findByAccount_NationalId(input.getNationalId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Invalid Credentials")
                        );
            if (!accountDetails.getAccount().isActive())
                throw new ResourceNotFoundException("Account not found");
            // generate token
            jwtToken = jwtService.generateToken(accountDetails);
        } catch (ResourceNotFoundException ex) {
            throw new ResourceNotFoundException("Account not found");
        } catch (Exception ex) {
            log.info(ex.getMessage() + "\n" + ex.getCause());
            throw new BadCredentialsException("Invalid Credentials");
        }
        return new TokenDto(
                jwtToken,
                jwtService.getExpirationTime().toString()
        );
    }

    public Account findMe() {
        // get Authentication Bean from context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = findByNationalId(authentication.getName());
        return account;
    }

    public Account changePassword(ChangePasswordDto changePasswordDto) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            changePasswordDto.getNationalId(),
                            changePasswordDto.getOldPassword()
                    )
            );
        } catch (Exception ex) {
            throw new AccessDeniedException("Your Credentials are not valid");
        }
        if (!authentication.isAuthenticated())
            throw new ValidationException("Your password is not correct");

        Account account = accountRepository.findByNationalId(
                authentication.getName()).orElseThrow(
                    () -> new ResourceNotFoundException("Account not found")
        );

        if (!account.isActive())
            throw new ResourceNotFoundException("Account not found");

        // update password
        account.setPassword(
                passwordEncoder.encode(
                        changePasswordDto.getNewPassword()));

        return accountRepository.save(account);
    }
    public ResponseEntity<Object> deActivate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Account account = findByNationalId(authentication.getName());
        try{
            account.setActive(false);
            accountRepository.save(account);
            return ResponseEntity.ok("Account successfully deleted");
        } catch (Exception ex) {
            throw new ValidationException(ex.getMessage());
        }
    }

    public Account findByNationalId(String id) {
        Account account =
                accountRepository.findByNationalId(id).orElseThrow(
                        () -> new ResourceNotFoundException("This account does not exists")
                );
        if(!account.isActive())
            throw new ResourceNotFoundException("Account not found");

        return account;
    }

}
