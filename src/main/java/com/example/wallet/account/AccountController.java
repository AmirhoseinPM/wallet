package com.example.wallet.account;

import com.example.wallet.account.helper.AccountValidator;
import com.example.wallet.account.helper.ChangePasswordDto;
import com.example.wallet.account.helper.LoginDto;
import com.example.wallet.account.helper.TokenDto;
import com.example.wallet.exception.ErrorResponseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/account")
public class AccountController {

    AccountService accountService;
    AccountValidator accountValidator;
    ErrorResponseService errorResponseService;

    @Autowired
    public AccountController(AccountService accountService,
                             AccountValidator accountValidator,
                             ErrorResponseService errorResponseService) {
        this.accountService = accountService;
        this.accountValidator = accountValidator;
        this.errorResponseService = errorResponseService;
    }

    @PostMapping(
            value = "/signup",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> signup(
            @RequestBody @Valid Account account,
            BindingResult bindingResult) {

        // validate user input
        account.setId(null);
        accountValidator.validate(account, bindingResult);

        if (bindingResult.hasErrors()) {
            // return ErrorResponse with error messages
            return errorResponseService.returnValidationError(bindingResult);

        }
        else
            // return created Account
            return ResponseEntity.ok(accountService.signup(account));
    }

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public TokenDto login(@Valid @RequestBody LoginDto loginAccountDto) {
        return accountService.login(loginAccountDto);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public Account getMe() {
        // get Account base on Authorization
        return accountService.findMe();
    }

    @PutMapping(value = "/password",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto,
                                        BindingResult result) {
        if (result.hasErrors()) {
            return errorResponseService.returnValidationError(result);
        }
        return ResponseEntity.ok(accountService.changePassword(changePasswordDto));
    }

    @DeleteMapping
    public ResponseEntity<Object> deleteAccount() {
        return accountService.deActivate();
    }

}
