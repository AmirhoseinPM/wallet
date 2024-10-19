package com.example.wallet.account.helper;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChangePasswordDto {

    @NotNull(message = "Password is required")
    @Size(min = 8, max=60, message = "Password contains 8 character at least")
    private String oldPassword;

    @NotNull(message = "Password is required")
    @Size(min = 8, max=60, message = "Password contains 8 character at least")
    private String newPassword;

    @NotNull(message = "National Id is required")
    @Size(max = 10, min = 10, message = "National id contains 10 number")
    private String nationalId;

    public ChangePasswordDto() {}

    public ChangePasswordDto(String oldPassword, String newPassword, String nationalId) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
        this.nationalId = nationalId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }
}
