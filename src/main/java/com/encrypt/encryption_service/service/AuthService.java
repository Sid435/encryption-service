package com.encrypt.encryption_service.service;

import com.encrypt.encryption_service.db.UserDeviceDetailsEntity;
import com.encrypt.encryption_service.db.UserEntity;
import com.encrypt.encryption_service.dto.response.AuthenticationResponse;
import com.encrypt.encryption_service.repository.UserDeviceDetailsRepository;
import com.encrypt.encryption_service.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

    private static final String REFRESH = "REFRESH";
    private static final String ACCESS = "ACCESS";
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private static final String TTYPE = "ttype";
    private static final String INVALID_TOKEN = "Invalid token";
    private static final String USER_LOGGED_OUT = "user logged out successfully";
    private static final String USER_NOT_FOUND = "User not found";
    private final UserDeviceDetailsRepository userDeviceDetailsRepository;

    public AuthenticationResponse register(UserEntity user) {
        Map<String, Object> accessExtraClaims = new HashMap<>();
        accessExtraClaims.put(TTYPE, ACCESS);
        accessExtraClaims.put("userId", user.getUserId());
        accessExtraClaims.put("userEmail", user.getEmail());
        var jwtToken = jwtService.generateAccessToken(accessExtraClaims, user);
        Map<String, Object> refreshExtraClaims = new HashMap<>();
        refreshExtraClaims.put(TTYPE, REFRESH);
        refreshExtraClaims.put("userId", user.getUserId());
        refreshExtraClaims.put("userEmail", user.getEmail());
        var refreshToken = jwtService.generateRefreshToken(refreshExtraClaims, user);
        return AuthenticationResponse.builder().accessToken(jwtToken).refreshToken(refreshToken).build();
    }


    public AuthenticationResponse refreshToken(HttpServletRequest request) throws Exception {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userId;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Invalid token, token not found in header");
            throw new Exception(INVALID_TOKEN);
        }
        refreshToken = authHeader.substring(7);
        userId = jwtService.extractRefreshTokenSubject(refreshToken);
        if (userId != null) {
            var user = this.userRepository.getUserByUserId(userId);
            if (user == null) {
                throw new Exception(USER_NOT_FOUND);
            }
            if (jwtService.isValidRefreshToken(refreshToken, user)
            ) {
                Map<String, Object> extraClaims = new HashMap<>();
                extraClaims.put(TTYPE, ACCESS);
                var accessToken = jwtService.generateAccessToken(extraClaims, user);
                var hashedToken = jwtService.getHashedTokenValue(accessToken);
                var hashedRefreshToken = jwtService.getHashedTokenValue(refreshToken);
                saveUserAccessToken(user, hashedToken, hashedRefreshToken);
                return AuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).build();

            } else {
                log.error("Token expired or revoked");
                throw new Exception("Token expired or revoked");
            }
        }
        throw new Exception(INVALID_TOKEN);
    }

    public void checkValidUserWithUserId(String userId) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)
                || !(authentication.getPrincipal() instanceof UserEntity userEntity)
                || !userEntity.getUserId().equals(userId)) {
            throw new Exception("Unauthorised Access");
        }
    }

    public String revokeUserTokens(UserIdDto userIdDto, HttpServletRequest request) throws Exception {
        log.info("revokeUserTokens called");
        checkValidUserWithUserId(userIdDto.getUserId());
//        check if user is present
        if (!StringUtils.hasText(userIdDto.getUserId())) {
            log.error("invalid userId");
            throw new Exception("invalid user Id");
        }
        UserEntity currentUser = userRepository.getUserByUserId(userIdDto.getUserId());
        if (currentUser == null) {
            log.error(USER_NOT_FOUND);
            throw new Exception(USER_NOT_FOUND);
        }
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new Exception(INVALID_TOKEN);
        }
        String accessToken = authHeader.substring(7);
        String hashedAccessToken = jwtService.getHashedTokenValue(accessToken);
        revokeAllUserTokens(currentUser, hashedAccessToken);
        log.info("revoked user tokens, user logged out successfully");
        return USER_LOGGED_OUT;
    }


    private void saveUserAccessToken(UserEntity user, String hashedAccessToken, String hashedRefreshToken) throws Exception {
        Optional<UserDeviceDetailsEntity> userDeviceDetails = userDeviceDetailsRepository.findByUserIdAndRefreshToken(user.getUserId(), hashedRefreshToken);
        if (userDeviceDetails.isPresent()) {
            UserDeviceDetailsEntity userDeviceDetailsEntity = userDeviceDetails.get();
            userDeviceDetailsEntity.setUserAccessToken(hashedAccessToken);
            userDeviceDetailsRepository.save(userDeviceDetailsEntity);
        } else {
            throw new Exception("Token validity expired or revoked");
        }
    }


    public void revokeAllUserTokens(UserEntity user, String accessToken) throws Exception {
        Optional<UserDeviceDetailsEntity> userDeviceDetails = userDeviceDetailsRepository
                .findByUserIdAndAccessToken(user.getUserId(), accessToken);
        if (userDeviceDetails.isPresent()) {
            UserDeviceDetailsEntity userDeviceDetailsEntity = userDeviceDetails.get();
            userDeviceDetailsEntity.setUserAccessToken(null);
            userDeviceDetailsEntity.setUserRefreshToken(null);
            userDeviceDetailsRepository.save(userDeviceDetailsEntity);
        } else {
            throw new Exception("Token validity expired or revoked");
        }

    }
}
