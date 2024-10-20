package com.example.wallet.account.helper;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class GenderConverter implements AttributeConverter<Gender, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Gender attribute) {
        if (attribute == null)
            return null;
        return attribute.getGender();
    }

    @Override
    public Gender convertToEntityAttribute(Integer dbData) {
        if (dbData == null)
            return null;
        return Stream.of(Gender.values())
                .filter(g -> dbData.equals(g.getGender()))
                .findFirst()
                .orElseThrow(
                        IllegalArgumentException::new
                );
    }
}
