package com.encrypt.encryption_service.dto.request;

import lombok.Data;

@Data
public class UserOnboardRequest {
    private String email;
    private String password;
    private String name;
}
