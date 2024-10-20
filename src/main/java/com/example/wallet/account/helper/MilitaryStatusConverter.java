package com.example.wallet.account.helper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class MilitaryStatusConverter implements AttributeConverter<MilitaryStatus, Integer> {
    @Override
    public Integer convertToDatabaseColumn(MilitaryStatus attribute) {
        if (attribute == null)
            return null;
        return attribute.getMilitaryStatus();
    }

    @Override
    public MilitaryStatus convertToEntityAttribute(Integer dbData) {
        if (dbData == null)
            return null;
        return Stream.of(MilitaryStatus.values())
                .filter(s -> dbData.equals(s.getMilitaryStatus()))
                .findFirst()
                .orElseThrow(
                        IllegalArgumentException::new
                );
    }
}
