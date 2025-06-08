package com.encrypt.encryption_service.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;

@Data
@Builder
public class FileDecodeResponse {
    ByteArrayResource decodeFile;
}
