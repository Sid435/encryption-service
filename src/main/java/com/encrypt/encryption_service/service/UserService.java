package com.encrypt.encryption_service.service;

import com.encrypt.encryption_service.db.Keys;
import com.encrypt.encryption_service.db.UserDeviceDetailsEntity;
import com.encrypt.encryption_service.db.UserEntity;
import com.encrypt.encryption_service.dto.request.UserOffBoardRequest;
import com.encrypt.encryption_service.dto.request.UserOnboardRequest;
import com.encrypt.encryption_service.dto.response.AuthenticationResponse;
import com.encrypt.encryption_service.dto.response.UserDetailsResponse;
import com.encrypt.encryption_service.dto.response.UserOffBoardResponse;
import com.encrypt.encryption_service.dto.response.UserOnboardResponse;
import com.encrypt.encryption_service.repository.KeysRepository;
import com.encrypt.encryption_service.repository.UserDeviceDetailsRepository;
import com.encrypt.encryption_service.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final JwtService jwtService;
    private final PasswordEncoderService passwordEncoderService;
    private final KeysRepository keysRepository;
    private final UserDeviceDetailsRepository deviceDetailsRepository;

    public UserOnboardResponse signIn(UserOnboardRequest userOnboardRequest) throws Exception {
        Optional<UserEntity> user = userRepository.getUserByEmail(userOnboardRequest.getEmail());

        boolean f = user.isPresent();

        UUID uuid = UUID.randomUUID();
        String userId = uuid.toString() + System.currentTimeMillis()/1000;
        Map<String, String> keyMap = generateUserRSAKey();
        String publicKey = keyMap.get("publicKey");
        String privateKey = keyMap.get("privateKey");
        UserEntity userEntity = f ? user.get() : UserEntity.builder()
                .userId(userId)
                .email(userOnboardRequest.getEmail())
                .publicKey(publicKey)
                .password(passwordEncoderService.encodePass(userOnboardRequest.getPassword())) // TODO: Encode
                .build();

        userRepository.save(userEntity);

        Keys keys = Keys.builder()
                .user(userEntity)
                .privateKey(privateKey)
                .build();
        keysRepository.save(keys);


        AuthenticationResponse authenticationResponse = authService.register(userEntity);
        UserDeviceDetailsEntity detailsEntity = UserDeviceDetailsEntity.builder()
                .userAccessToken(jwtService.getHashedTokenValue(authenticationResponse.getAccessToken()))
                .userRefreshToken(jwtService.getHashedTokenValue(authenticationResponse.getRefreshToken()))
                .deviceToken(userId.substring(0, 5))
                .user(userEntity)
                .lastLoginTime(LocalDateTime.now())
                .build();

        deviceDetailsRepository.save(detailsEntity);
        return UserOnboardResponse.builder()
                .userId(userId)
                .accessToken(authenticationResponse.getAccessToken())
                .refreshToken(authenticationResponse.getRefreshToken())
                .build();
    }
    public Boolean validateToken(String token) {
        String subject = jwtService.extractAccessTokenSubject(token);
        Optional<UserEntity> byEmail = userRepository.getUserByEmail(subject.toLowerCase());
        UserEntity idExists = byEmail.orElse(null);
        if (idExists == null) {
            return false;
        }
        return jwtService.isValidAccessToken(token, idExists);
    }

    public UserOffBoardResponse signOff(UserOffBoardRequest userOffBoardRequest){
        try{
            UserEntity userByUserId = userRepository.getUserByUserId(userOffBoardRequest.getUserId());
            Keys keysByUserId = keysRepository.findByUserId(userOffBoardRequest.getUserId());
            if (userByUserId == null) {
                throw new UsernameNotFoundException("User not found");
            }
            userRepository.delete(userByUserId);

            return UserOffBoardResponse.builder()
                    .userId(userOffBoardRequest.getUserId())
                    .build();
        }
        catch (Exception e){
            log.error("Error occurred while offboarding user ", e);
            return null;
        }
    }

    public List<UserDetailsResponse> getAllUsers() {
        List<UserEntity> users = userRepository.getAllUsers();
        return users.stream()
                .map(userEntity ->
                    UserDetailsResponse.builder()
                            .userId(userEntity.getUserId())
                            .email(userEntity.getEmail())
                            .build()
                ).toList();
    }

    public Map<String, String> generateUserRSAKey() throws Exception {

        try{
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();

            String privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            String publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());

            return Map.of(
                    "privateKey", privateKey,
                    "publicKey", publicKey
            );
        } catch (Exception e) {
            log.error("Error generating RSA key pair: {}", e.getMessage());
            throw new Exception("Error generating RSA key pair", e);
        }
    }
}
