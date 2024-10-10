package com.example.wallet.account;

import com.example.wallet.jwt.JwtService;
import com.example.wallet.exception.ResourceNotFoundException;
import com.example.wallet.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
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

    private Logger log = LoggerFactory.getLogger(AccountService.class);


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

    public Account signup(Account account) {

        // encrypt account password before saving it
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        AccountDetails accountDteails = new AccountDetails(account);
        // try to save account and return token and account info
        try {
            accountDteails = accountDetailsRepository.save(accountDteails);
            account = accountDteails.getAccount();
            log.info(accountDteails.toString());
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
            // generate token
            jwtToken = jwtService.generateToken(
                    accountDetailsRepository.findByAccount_NationalId(input.getNationalId())
                            .orElseThrow(
                                    () -> new ResourceNotFoundException("Invalid Credentials")
                            ));
        } catch (Exception ex) {
            log.info(ex.getMessage() + "\n" + ex.getCause());
            throw new ResourceNotFoundException("Invalid Credentials");
        }
        return new TokenDto(
                jwtToken,
                jwtService.getExpirationTime().toString()
        );
    }

    public Account findMe() {
        // get Authentication Bean from context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return findByNationalId(authentication.getName());
    }

    public Account changePassword(ChangePasswordDto changePasswordDto) {

        Account account = findByNationalId(changePasswordDto.getNationalId());

        // check old password matches
        String decryptedOldPass = passwordEncoder.encode(
                changePasswordDto.getOldPassword());
        if (!account.getPassword().equals(decryptedOldPass))
            throw new ValidationException("Your password is not correct");

        // update password
        account.setPassword(
                passwordEncoder.encode(
                        changePasswordDto.getNewPassword()));

        return accountRepository.save(account);
    }

    public Account findByNationalId(String id) {
        return accountRepository.findByNationalId(id).orElseThrow(
                () -> new ResourceNotFoundException("This account does not exists")
        );
    }

    public Account save(Account account) {
        return accountRepository.save(account);
    }
//
//    public List<Account> findAll() {
//        return (List<Account> ) accountRepository.findAll();
//    }
}
