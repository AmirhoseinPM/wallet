package com.example.wallet.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum Gender {
    MALE(1), FEMALE(2), OTHERS(3);

    final int gender;
    Gender(int gender) {
        this.gender = gender;
    }
    public int getGender() {
        return gender;
    }

    @JsonValue
    public String getGenderAsString() {
        return this.name();
    }

    @JsonCreator
    public static Gender fromValue(String value) {
        return Stream.of(Gender.values())
                .filter(g -> g.name().equals(value.toUpperCase()))
                .findFirst()
                .orElse(null);
    }


}
