package com.example.wallet.account;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
public class AccountDetails implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    @NotNull
    @OneToOne(cascade = CascadeType.ALL,
              fetch = FetchType.EAGER
    )
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;

    // constructors
    public AccountDetails() {}

    public AccountDetails(Account account) {
        this.account = account;
    }

    // override methods

    @Override
    public boolean isAccountNonExpired() {
        return account.isActive();
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return account.isActive();
    }

    @Override
    public boolean isEnabled() {
        return account.isActive();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getNationalId();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "AccountDteails{" +
                "id=" + id +
                ", account=" + account.toString() +
                '}';
    }
}
