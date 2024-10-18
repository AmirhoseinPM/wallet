package com.example.wallet.transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TransactionDto {

    private Long walletId;

    @NotNull(message = "withdrawal field is required")
    private Boolean isWithdrawal;

    @NotNull(message = "amount field is required")
    @Min(value = 1_000, message = "Minimum value for amount is 1000")
    private Long amount;

    @Size(max = 255, message = "description must be less than or equal to 255 character")
    private String description;

    // constructor
    public TransactionDto() {}
    public TransactionDto(Long walletId, boolean isWithdrawal, Long amount, String description) {
        this.walletId = walletId;
        this.isWithdrawal = isWithdrawal;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Boolean isWithdrawal() {
        return isWithdrawal;
    }

    public void setWithdrawal(boolean withdrawal) {
        isWithdrawal = withdrawal;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TransactionDto{" +
                "walletId=" + walletId +
                ", isWithdrawal=" + isWithdrawal +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}
