package com.encrypt.encryption_service.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileEncodeResponse {
    private String receiverEmail;
    private String fileEncode; // This is the encrypted file
}
