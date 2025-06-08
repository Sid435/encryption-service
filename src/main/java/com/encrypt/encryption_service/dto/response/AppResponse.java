package com.encrypt.encryption_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppResponse<T> {
    private String requestUri;
    private T data;
    private int status;
    private String error;
}
