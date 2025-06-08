package com.encrypt.encryption_service.service;

import com.encrypt.encryption_service.service.encrypt_decrypt.Crypt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;

@Slf4j
@Component
public class CryptImpl implements Crypt {
    @Override
    public byte[] encrypt(byte[] file, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        SecretKey aesKey = getAesKey();

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedFile = aesCipher.doFinal(file);

        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

        ByteBuffer buffer = ByteBuffer.allocate(4 + encryptedAesKey.length + encryptedFile.length);
        buffer.putInt(encryptedAesKey.length);
        buffer.put(encryptedAesKey);
        buffer.put(encryptedFile);

        return buffer.array();
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        ByteBuffer buffer = ByteBuffer.wrap(encryptedData);

        int aesKeyLength = buffer.getInt();  // first 4 bytes
        byte[] encryptedAesKey = new byte[aesKeyLength];
        buffer.get(encryptedAesKey);

        byte[] encryptedFileData = new byte[buffer.remaining()];
        buffer.get(encryptedFileData);

        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey);
        return aesCipher.doFinal(encryptedFileData);
    }

    public SecretKey getAesKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
}
