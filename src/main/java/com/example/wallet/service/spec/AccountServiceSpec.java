package com.example.wallet.service.spec;

import com.example.wallet.domain.entity.Account;
import com.example.wallet.dto.ChangePasswordDto;
import com.example.wallet.dto.LoginDto;
import com.example.wallet.dto.TokenDto;
import com.example.wallet.exception.ValidationException;
import org.springframework.http.ResponseEntity;

public interface AccountServiceSpec {
    Account signup(Account account) throws ValidationException;
    TokenDto login(LoginDto input);
    Account findMe();
    Account changePassword(ChangePasswordDto changePasswordDto);
    ResponseEntity<Object> deActivate();
    Account findByNationalId(String id);
}
