package com.encrypt.encryption_service.service;

import com.encrypt.encryption_service.db.Keys;
import com.encrypt.encryption_service.db.UserEntity;
import com.encrypt.encryption_service.dto.request.FileDecodeRequest;
import com.encrypt.encryption_service.dto.response.FileDecodeResponse;
import com.encrypt.encryption_service.dto.response.FileEncodeResponse;
import com.encrypt.encryption_service.repository.KeysRepository;
import com.encrypt.encryption_service.repository.UserRepository;
import com.encrypt.encryption_service.service.email_service.EmailImpl;
import com.encrypt.encryption_service.service.encrypt_decrypt.Crypt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {
    private final UserRepository userRepository;
    private final KeysRepository keysRepository;
    private final Crypt crypt;
    private final EmailImpl emailDao;

    public FileEncodeResponse encodeFile(String receiverEmail,MultipartFile file) {
        try{
            log.info("Fetching intended user details");
            if(receiverEmail == null || receiverEmail.isBlank()){
                throw new Exception("User Details Invalid");
            }
            Optional<UserEntity> user = userRepository.getUserByEmail(receiverEmail);
            if(user.isEmpty()){
                throw new Exception("User not present");
            }
            PublicKey publicKey = user.get().getPublicKey();
            if(file.isEmpty()) {
                throw new Exception("File not present");
            }
            byte[] fileByte = crypt.encrypt(file.getBytes(), publicKey);
            String fileBytes = Base64.getEncoder().encodeToString(fileByte);

            emailDao.sendFileEmail(file.getName(), fileByte, receiverEmail);

            return FileEncodeResponse.builder()
                    .receiverEmail(user.get().getEmail())
                    .fileEncode(fileBytes).build();
        }
        catch (Exception e){
            log.info(e.getMessage());
            return null;
        }
    }

    public FileDecodeResponse decryptFile(FileDecodeRequest request, MultipartFile file) {
        if (request.getUserId() == null || request.getUserId().isBlank()) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        Keys keysOpt = keysRepository.findByUserId(request.getUserId());
        if (keysOpt == null) {
            throw new IllegalArgumentException("User not present");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File not present");
        }

        try {
            PrivateKey privateKey = keysOpt.getPrivateKey();

            byte[] decryptedContent = crypt.decrypt(file.getBytes(), privateKey);

            return FileDecodeResponse.builder()
                    .decodeFile(new ByteArrayResource(decryptedContent))
                    .build();

        } catch (Exception e) {
            log.error("Failed to decrypt file: {}", e.getMessage(), e);
            throw new RuntimeException("File decryption failed");
        }
    }


}
