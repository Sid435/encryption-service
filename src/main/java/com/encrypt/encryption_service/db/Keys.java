package com.encrypt.encryption_service.db;

import com.encrypt.encryption_service.converter.StringEncryptDecryptConverter;
import jakarta.persistence.*;
import lombok.*;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Entity(name = "user_keys")
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Keys {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    @ToString.Exclude
    UserEntity user;

    @Convert(converter = StringEncryptDecryptConverter.class)
    @Lob
    String privateKey;

    public PrivateKey getPrivateKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(this.privateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to convert stored private key", e);
        }
    }
}
