package com.example.wallet.enums;

public enum Gender {
    MALE(1), FEMALE(2), OTHERS(3);

    final int gender;
    Gender(int gender) {
        this.gender = gender;
    }
    public int getGender() {
        return gender;
    }

}
