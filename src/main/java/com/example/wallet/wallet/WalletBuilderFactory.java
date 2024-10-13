package com.example.wallet.wallet;

import com.example.wallet.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class WalletBuilderFactory implements WalletFactory{

    @Override
    public Wallet getNewWallet(WalletDto walletDto, String nationalId) {
        Wallet wallet;
        try {
            long nationalIdLong = Long.parseLong(nationalId);
            wallet = new Wallet();
            // set initial balance
            wallet.setBalance((double) walletDto.getInitialBalance());
            // set title
            wallet.setTitle(walletDto.getTitle());
            // set sheba identifier
            String sheba = generateSheba(nationalIdLong);
            wallet.setSheba(sheba);
            // set number identifier
            String number = getNumberFromSheba(sheba);
            wallet.setNumber(number);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ValidationException("Error in building wallet");
        }
        return wallet;
    }


    public String generateSheba(long inputSeed) {
        // generate unique 22 digits account number
        String number;
        Random random = new Random(inputSeed);
        long randomBase  = Math.abs(random.nextLong());
        long currentTimeMillis = System.currentTimeMillis();
        long result = randomBase + currentTimeMillis;
        number = String.valueOf(result);
        number = number.substring(0, 18);

        return "IR01" + "010" + "0" + number;
    }

    public String getNumberFromSheba(String sheba) {
        return sheba.substring(4);
    }


}
