package com.example.wallet.wallet.helper;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WalletUpdateDto {

    private long id;

    @NotNull(message = "title is required.")
    @Size(min = 1, max = 60, message = "title length must between 1 and 60")
    private String title;


    // Constructor, Getter Setter

    public WalletUpdateDto() {}

    public WalletUpdateDto(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
