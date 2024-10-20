package com.example.wallet.service;

import com.example.wallet.dto.WalletDto;
import com.example.wallet.entity.Wallet;

public interface WalletFactory {
    Wallet getNewWallet(WalletDto walletDto, String nationalId);
}
