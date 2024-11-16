package com.example.wallet.controller;

import com.example.wallet.dto.ChangePasswordDto;
import com.example.wallet.dto.LoginDto;
import com.example.wallet.dto.TokenDto;
import com.example.wallet.domain.entity.Account;
import com.example.wallet.service.spec.AccountServiceSpec;
import com.example.wallet.service.spec.ErrorResponseServiceSpec;
import com.example.wallet.validation.AccountValidator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/account")
@CrossOrigin(origins = "*")
public class AccountController {

    AccountServiceSpec accountService;
    AccountValidator accountValidator;
    ErrorResponseServiceSpec errorResponseService;

    @Autowired
    public AccountController(AccountServiceSpec accountService,
                             AccountValidator accountValidator,
                             ErrorResponseServiceSpec errorResponseService) {
        this.accountService = accountService;
        this.accountValidator = accountValidator;
        this.errorResponseService = errorResponseService;
    }


    @PostMapping(
            value = "/signup",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
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
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public ResponseEntity<Object> deleteAccount() {
        return accountService.deActivate();
    }

}
