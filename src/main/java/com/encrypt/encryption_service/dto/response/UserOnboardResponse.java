package com.encrypt.encryption_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserOnboardResponse {
    private String userId;

    private String accessToken;
    private String refreshToken;
}
