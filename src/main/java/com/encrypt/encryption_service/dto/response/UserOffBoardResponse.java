package com.encrypt.encryption_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserOffBoardResponse {
    private String userId;
}
