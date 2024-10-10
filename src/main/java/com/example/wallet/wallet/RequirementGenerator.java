package com.example.wallet.wallet;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RequirementGenerator {

    public String generateSheba(long inputSeed) {
        // generate unique 22 digits account number
        String number = "";
        Random random = new Random(inputSeed);
        long randomBase  = Math.abs(random.nextLong());
        long currentTimeMillis = System.currentTimeMillis();
        long result = randomBase + currentTimeMillis;
        number = String.valueOf(result);
        number = number.substring(0, 18);
        return getShebaFromAccountNumber(number);
    }

    public String getNumberFromSheba(String sheba) {
        return sheba.substring(4);
    }

    private static String getShebaFromAccountNumber(String number) {
        // generate sheba based on account number
        return "IR01" + "010" + "0" + number;
    }

    private static long byteToLong(byte[] encodedHash) {
        long result = 0;
        for(int i = 0; i < Math.min(encodedHash.length, 8); i++) {
            result |= (encodedHash[i] & 0xffl) << (8 * i);
        }
        return result;
    }
}
