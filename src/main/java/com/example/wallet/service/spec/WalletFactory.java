package com.example.wallet.service.spec;

import com.example.wallet.dto.WalletDto;
import com.example.wallet.domain.entity.Wallet;

public interface WalletFactory {
    Wallet getNewWallet(WalletDto walletDto, String nationalId);
}
