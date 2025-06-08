package com.encrypt.encryption_service.controller;

import com.encrypt.encryption_service.dto.request.FileDecodeRequest;
import com.encrypt.encryption_service.dto.response.AppResponse;
import com.encrypt.encryption_service.dto.response.FileDecodeResponse;
import com.encrypt.encryption_service.dto.response.FileEncodeResponse;
import com.encrypt.encryption_service.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping(value = "/encode", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppResponse<FileEncodeResponse>> encodeFile(@RequestParam(required = true, value = "file") MultipartFile file, @RequestParam(required = true, value = "receiverEmail") String receiverEmail){

        AppResponse<FileEncodeResponse> appResponse = AppResponse.
                <FileEncodeResponse>builder()
                .data(fileService.encodeFile(receiverEmail, file))
                .build();

        return new ResponseEntity<>(appResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/decrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AppResponse<FileDecodeResponse>> decodeFile(@RequestParam(required = true, value = "file") MultipartFile file, @RequestBody FileDecodeRequest fileEncodeRequest){

        AppResponse<FileDecodeResponse> appResponse = AppResponse.
                <FileDecodeResponse>builder()
                .data(fileService.decryptFile(fileEncodeRequest, file))
                .build();

        return new ResponseEntity<>(appResponse, HttpStatus.OK);
    }
}
