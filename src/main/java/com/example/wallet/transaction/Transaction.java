package com.example.wallet.transaction;

import com.example.wallet.wallet.Wallet;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createdAt;

    @ManyToOne(cascade = {
            CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    private boolean isWithdrawal;

    @NotNull(message = "\"amount\" field is required")
    @Min(value = 1, message = "Minimum withdrawal amount is 1")
    private Long amount;


    private String descreption;

    // Constructors

    public Transaction() {}

    public Transaction(LocalDateTime createdAt, Wallet wallet,
                       boolean isWithdrawal, long amount,
                       String descreption) {
        this.createdAt = createdAt;
        this.wallet = wallet;
        this.isWithdrawal = isWithdrawal;
        this.amount = amount;
        this.descreption = descreption;
    }

    // Getter and Setter

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public boolean isWithdrawal() {
        return isWithdrawal;
    }

    public void setWithdrawal(boolean withdrawal) {
        isWithdrawal = withdrawal;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getDescreption() {
        return descreption;
    }

    public void setDescreption(String descreption) {
        this.descreption = descreption;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", wallet=" + wallet +
                ", isWithdrawal=" + isWithdrawal +
                ", amount=" + amount +
                '}';
    }
}
