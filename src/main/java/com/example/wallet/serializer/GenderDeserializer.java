package com.example.wallet.serializer;

import com.example.wallet.enums.Gender;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.yaml.snakeyaml.util.EnumUtils;

import java.io.IOException;

public class GenderDeserializer extends JsonDeserializer<Gender> {
    @Override
    public Gender deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        String input = p.getText();
        Gender gender = null;
        try {
            gender = EnumUtils.findEnumInsensitiveCase(Gender.class, input);
        } catch (Exception e) {
            System.out.println("gender not valid"); // this line printed when input is "fema"
            // but throwing below exception get "failed to raed request" 400 badRequest detail, and user can not see appropriate message.
//            throw new IllegalArgumentException("gender not valid, choices are male, female, others.");
        }
        // therefor return null to get appropriate message in @NotNull validation
        return gender;
    }
}
