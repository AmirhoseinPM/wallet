package com.example.wallet.account;

import com.example.wallet.exception.ErrorResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/account")
public class AccountController {

    AccountService accountService;
    AccountValidator accountValidator;

    @Autowired
    public AccountController(AccountService accountService, AccountValidator accountValidator) {
        this.accountService = accountService;
        this.accountValidator = accountValidator;
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
            List<String> errors = bindingResult.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("404", "validation failed", errors));

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

//
//    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
//    public List<Account> getAll() {
//        // get Account base on Authorization
//        return accountService.findAll();
//    }


    @PostMapping(
            value = "/password",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> login(@Valid @RequestBody ChangePasswordDto changePasswordDto, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getAllErrors()
                    .stream()
                    .map(e -> e.getDefaultMessage())
                    .toList();
            return ResponseEntity.ok(
                    new ErrorResponse("400", "bad request", errors));
        }
        return ResponseEntity.ok(accountService.changePassword(changePasswordDto));
    }

}
