package com.encrypt.encryption_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Getter
@Setter
@Configuration
public class AppConfig {

    @Value("${application.security.jwt.access-token.private-key}")
    private String accessPrivateKeyPath;

    @Value("${application.security.jwt.access-token.public-key}")
    private String accessPublicKeyPath;

    @Value("${application.security.jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${application.security.jwt.refresh-token.private-key}")
    private String refreshPrivateKeyPath;

    @Value("${application.security.jwt.refresh-token.public-key}")
    private String refreshPublicKeyPath;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${encryption.aes.key}")
    private String aesKey;

    @Value("${spring.mail.username}")
    private String emailSender;

    public SecretKey getAesKey() throws IOException {
        try (InputStream is = new ClassPathResource(aesKey.replace("classpath:", "")).getInputStream()) {
            byte[] encoded = is.readAllBytes();

            byte[] keyBytes = Base64.getDecoder().decode(new String(encoded, StandardCharsets.UTF_8).trim());

            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid AES key length: " + keyBytes.length + " bytes");
            }

            return new SecretKeySpec(keyBytes, "AES");
        }
    }
    public String getAccessPrivateKey() {
        return readFromClasspath(accessPrivateKeyPath, true);
    }

    public String getAccessPublicKey() {
        return readFromClasspath(accessPublicKeyPath, true);
    }

    public String getRefreshPrivateKey() {
        return readFromClasspath(refreshPrivateKeyPath, true);
    }

    public String getRefreshPublicKey() {
        return readFromClasspath(refreshPublicKeyPath, true);
    }

    private String readFromClasspath(String path, boolean isPem) {
        try (InputStream is = new ClassPathResource(path.replace("classpath:", "")).getInputStream()) {
            String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            return isPem ? cleanPemKey(pem) : pem;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PEM file from: " + path, e);
        }
    }

    private String cleanPemKey(String pem) {
        return pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", ""); // remove newlines, tabs, etc.
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}