package com.encrypt.encryption_service.converter;

import com.encrypt.encryption_service.config.AppConfig;
import jakarta.persistence.AttributeConverter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

public abstract class AESEncryptionConverter<X> implements AttributeConverter<X, String> {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12; // 96 bits for GCM
    private static final int TAG_LENGTH = 128; // bits

    private final AppConfig appConfig;

    public AESEncryptionConverter(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public String convertToDatabaseColumn(X attribute) {
        if (attribute == null) return null;

        try {
            String plainText = convertEntityAttributeToString(attribute);
            byte[] iv = generateIV();
            SecretKey key = appConfig.getAesKey();

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes());

            // Store IV + CipherText in Base64
            byte[] ivAndCipherText = new byte[IV_LENGTH + cipherText.length];
            System.arraycopy(iv, 0, ivAndCipherText, 0, IV_LENGTH);
            System.arraycopy(cipherText, 0, ivAndCipherText, IV_LENGTH, cipherText.length);

            return Base64.getEncoder().encodeToString(ivAndCipherText);
        } catch (Exception e) {
            throw new IllegalStateException("Could not encrypt data", e);
        }
    }

    @Override
    public X convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;

        try {
            byte[] decoded = Base64.getDecoder().decode(dbData);
            byte[] iv = new byte[IV_LENGTH];
            byte[] cipherText = new byte[decoded.length - IV_LENGTH];

            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);
            System.arraycopy(decoded, IV_LENGTH, cipherText, 0, cipherText.length);

            SecretKey key = appConfig.getAesKey();

            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plainText = cipher.doFinal(cipherText);
            return convertStringToEntityAttribute(new String(plainText));
        } catch (Exception e) {
            throw new IllegalStateException("Could not decrypt data", e);
        }
    }

    private byte[] generateIV() {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // Abstract methods to handle conversion between String <-> X
    protected abstract String convertEntityAttributeToString(X attribute);
    protected abstract X convertStringToEntityAttribute(String dbData);
}