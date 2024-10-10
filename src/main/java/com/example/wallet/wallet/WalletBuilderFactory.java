package com.example.wallet.wallet;

import com.example.wallet.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WalletBuilderFactory implements WalletFactory{
    @Autowired
    RequirementGenerator generator;

    @Override
    public Wallet getNewWallet(WalletDto walletDto, String nationalId) {
        Wallet wallet = null;
        try {
            long nationalIdLong = Long.parseLong(nationalId);
            wallet = new Wallet();
            // set initial balance
            wallet.setBalance((double) walletDto.getInitialBalance());
            // set title
            wallet.setTitle(walletDto.getTitle());
            // set sheba identifier
            String sheba = generator.generateSheba(nationalIdLong);
            wallet.setSheba(sheba);
            // set number identifier
            String number = generator.getNumberFromSheba(sheba);
            wallet.setNumber(number);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("Error in building wallet");
        }
        return wallet;
    }
}
