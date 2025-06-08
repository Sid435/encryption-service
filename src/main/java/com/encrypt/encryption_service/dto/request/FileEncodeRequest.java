package com.encrypt.encryption_service.dto.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FileEncodeRequest {
    String userId;
    String receiverEmail;
}
