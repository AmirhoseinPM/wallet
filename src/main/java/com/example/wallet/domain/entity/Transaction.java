package com.example.wallet.domain.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @CreatedDate
    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private LocalDateTime createdAt;

    @CreatedBy
    @OneToOne
    private Account createdBy;

    @ManyToOne(cascade = {
            CascadeType.DETACH, CascadeType.MERGE,
            CascadeType.PERSIST, CascadeType.REFRESH
    })
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @NotNull(message = "\"isWithdraw\" field is required")
    private boolean isWithdrawal;

    @NotNull(message = "\"amount\" field is required")
    @Min(value = 1, message = "Minimum withdrawal amount is 1")
    private Long amount;


    @Size(max = 255, message = "description must be less than or equal to 255 character")
    private String description;

    // Constructors

    public Transaction() {}

    public Transaction(LocalDateTime createdAt, Wallet wallet,
                       boolean isWithdrawal, long amount,
                       String description) {
        this.createdAt = createdAt;
        this.wallet = wallet;
        this.isWithdrawal = isWithdrawal;
        this.amount = amount;
        this.description = description;
    }

    // Getter and Setter


    public Account getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Account createdBy) {
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
        return "Transaction{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                ", wallet=" + wallet +
                ", isWithdrawal=" + isWithdrawal +
                ", amount=" + amount +
                '}';
    }
}
