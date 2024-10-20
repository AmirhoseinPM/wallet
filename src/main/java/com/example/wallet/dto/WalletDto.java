package com.example.wallet.dto;

import jakarta.validation.constraints.Min;

public class WalletDto {

    private String title = "";
    @Min(value = 15000, message = "Initial balance minimum is 15,000")
    private long initialBalance;


    public long getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(long initialBalance) {
        this.initialBalance = initialBalance;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
