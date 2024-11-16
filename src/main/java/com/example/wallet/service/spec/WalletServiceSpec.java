package com.example.wallet.service.spec;

import com.example.wallet.domain.entity.Wallet;
import com.example.wallet.dto.WalletDto;
import com.example.wallet.dto.WalletUpdateDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WalletServiceSpec {
    Wallet get(Long id);
    List<Wallet> getAll();
    Wallet create(WalletDto walletDto);
    Wallet update(WalletUpdateDto walletUpdateDto);
    ResponseEntity<Object> delete(Long id);
}
