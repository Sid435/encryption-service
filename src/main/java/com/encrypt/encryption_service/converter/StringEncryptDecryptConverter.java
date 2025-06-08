package com.encrypt.encryption_service.converter;

import com.encrypt.encryption_service.config.AppConfig;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Component
@Converter
public class StringEncryptDecryptConverter extends AESEncryptionConverter<String>{

    public StringEncryptDecryptConverter(AppConfig appConfig) {
        super(appConfig);
    }

    @Override
    protected String convertEntityAttributeToString(String attribute) {
        return attribute;
    }

    @Override
    protected String convertStringToEntityAttribute(String dbData) {
        return dbData;
    }
}
