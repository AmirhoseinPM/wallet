package com.example.wallet.account.helper;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class LoginDto {
    @NotNull(message = "National Id is required")
    @Size(max = 10, min = 10, message = "National id contains 10 number")
    private String nationalId;

    @NotNull(message = "Password is required")
    @Size(min = 8, max=60, message = "Password contains 8 character at least")
    private String password;


    // Getter and Setter

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
