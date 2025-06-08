package com.encrypt.encryption_service.service;

import com.encrypt.encryption_service.config.AppConfig;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    public static final String HASH_ALGO = "SHA3-256";
    private final AppConfig appConfig;

    public JwtService(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    //generate tokens
    public String generateAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildAccessToken(extraClaims, userDetails, appConfig.getAccessTokenExpiration());
    }

    private String buildAccessToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey(appConfig.getAccessPrivateKey()), SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildRefreshToken(extraClaims, userDetails, appConfig.getRefreshTokenExpiration());
    }


    private String buildRefreshToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getPrivateKey(appConfig.getRefreshPrivateKey()), SignatureAlgorithm.RS256)
                .compact();
    }

    private PrivateKey getPrivateKey(String privateKey) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            return kf.generatePrivate(keySpecPKCS8);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Exception while retrieving private Key for signing JWT, ", e);
        }
        return null;
    }

    private PublicKey getPublicKey(String publicKey) {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey));
            return kf.generatePublic(keySpecX509);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("Exception while retrieving public Key for signing JWT, ", e);
        }
        return null;
    }

    public String extractAccessTokenSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractAccessTokenExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Object extractClaim(String token, String claimKey) {
        final Claims claims = extractAllClaims(token);
        return claims.get(claimKey);
    }

    public Claims extractAllClaims(String token) {

        return Jwts.parserBuilder().setSigningKey(getPublicKey(appConfig.getAccessPublicKey())).build().parseClaimsJws(token).getBody();
    }

    public String extractRefreshTokenSubject(String token) {
        return extractRefreshClaim(token, Claims::getSubject);
    }

    private Date extractRefreshTokenExpiration(String token) {
        return extractRefreshClaim(token, Claims::getExpiration);
    }


    private <T> T extractRefreshClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllRefreshClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllRefreshClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getPublicKey(appConfig.getRefreshPublicKey())).build().parseClaimsJws(token).getBody();
    }


    public boolean isValidAccessToken(String token, UserDetails userDetails) {
        final String username = extractAccessTokenSubject(token);
        return (username.equals(userDetails.getUsername())) && !isAccessTokenExpired(token);
    }

    public boolean isValidRefreshToken(String token, UserDetails userDetails) {
        final String username = extractRefreshTokenSubject(token);
        return (username.equals(userDetails.getUsername())) && !isRefreshTokenExpired(token);
    }

    public boolean isAccessTokenExpired(String token) {
        try {
            return extractAccessTokenExpiration(token).before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    private boolean isRefreshTokenExpired(String token) {
        return extractRefreshTokenExpiration(token).before(new Date());
    }

    public String getHashedTokenValue(String token) throws Exception {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(HASH_ALGO);
        } catch (NoSuchAlgorithmException e) {
            log.error("Error while hashing token", e);
            throw new Exception("Error while hashing token");
        }
        final byte[] hashbytes = digest.digest(
                token.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashbytes);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte h : hash) {
            String hex = Integer.toHexString(0xff & h);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }


    public String extractTokenTypeFromToken(String authHeader) throws Exception {
        try {
            SignedJWT signedJWT = SignedJWT.parse(authHeader);
            String ttype = signedJWT.getJWTClaimsSet().getStringClaim("ttype");
            return Objects.requireNonNull(ttype);
        } catch (Exception e) {
            throw new Exception("Token is not valid");
        }
    }
}
