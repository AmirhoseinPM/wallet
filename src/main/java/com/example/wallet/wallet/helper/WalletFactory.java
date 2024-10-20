package com.example.wallet.wallet.helper;

import com.example.wallet.wallet.Wallet;

public interface WalletFactory {
    Wallet getNewWallet(WalletDto walletDto, String nationalId);
}
