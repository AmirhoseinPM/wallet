package com.example.wallet.enums;

import com.example.wallet.exception.ValidationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.stream.Stream;

public enum MilitaryStatus {
    INCLUDED(1), EXCUSED(2), FINISHED(3), ABSENCE(4);

    final int militaryStatus;
    MilitaryStatus(int militaryStatus) {
        this.militaryStatus = militaryStatus;
    }
    public int getMilitaryStatus() {
        return militaryStatus;
    }

    @JsonValue
    public String getMilitaryAsString() {
        return this.name();
    }

    @JsonCreator
    public static MilitaryStatus fromValue(String value) {
        return Stream.of(MilitaryStatus.values())
                .filter(m -> m.name().equals(value.toUpperCase()))
                .findFirst()
                .orElseThrow(
                        () -> new ValidationException("militaryStatus choices are " +
                                Arrays.toString(MilitaryStatus.values())));
    }


}
