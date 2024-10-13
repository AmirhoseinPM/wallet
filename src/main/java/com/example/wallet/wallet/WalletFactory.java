package com.example.wallet.wallet;

public interface WalletFactory {
    Wallet getNewWallet(WalletDto walletDto, String nationalId);
}
