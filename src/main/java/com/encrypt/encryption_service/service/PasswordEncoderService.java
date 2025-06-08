package com.encrypt.encryption_service.service;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordEncoderService {

    private final PasswordEncoder encode;


    public String encodePass(String pass) {
        return encode.encode(pass);
    }

    public Boolean matchPass(String pass, String encPass) {
        return encode.matches(pass, encPass);
    }
}

