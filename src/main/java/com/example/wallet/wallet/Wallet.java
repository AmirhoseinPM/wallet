package com.example.wallet.wallet;

import com.example.wallet.account.Account;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "number", "sheba"})
})
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "title is required")
    @Size(min = 0, max = 60)
    private String title;

    @ManyToOne(cascade = {
                CascadeType.DETACH, CascadeType.MERGE,
                CascadeType.PERSIST, CascadeType.REFRESH}
    )
    @JoinColumn(name="account_id") // this name used in wallet table for person column name,
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // this access prevent Loop in reading person object.
    private Account account; // this field name used in mappedBy parameter of OneToMany in Person table

    @Min(value = 15000, message = "Initial balance minimum is 15,000")
    private Double balance;

    @NotNull(message = "Account number is required")
    @Size(min = 22, max = 22, message = "Account number must be 22 digits")
    @Pattern(regexp = "^[0-9]{22}$", message = "Account number must be 22 digits")
    private String number;

    @NotNull(message = "Sheba number is required")
    @Size(min = 26, max = 26, message = "Account sheba must be 26 char")
    @Pattern(regexp = "^IR[0-9]{24}$", message = "Account sheba must be 26 char")
    private String sheba;

    private boolean isActive = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
        account.addWallet(this);
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSheba() {
        return sheba;
    }

    public void setSheba(String sheba) {
        this.sheba = sheba;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "id=" + id +
                ", account=" + account +
                ", balance=" + balance +
                ", number='" + number + '\'' +
                ", sheba='" + sheba + '\'' +
                '}';
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
