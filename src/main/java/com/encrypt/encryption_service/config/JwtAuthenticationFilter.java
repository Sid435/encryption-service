package com.encrypt.encryption_service.config;

import com.encrypt.encryption_service.db.UserDeviceDetailsEntity;
import com.encrypt.encryption_service.db.UserEntity;
import com.encrypt.encryption_service.repository.UserDeviceDetailsRepository;
import com.encrypt.encryption_service.repository.UserRepository;
import com.encrypt.encryption_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //TODO : Complete this
    private final UserRepository userRepository;
    private final UserDeviceDetailsRepository userDeviceDetailsRepository;
    private final JwtService jwtService;

    private static final List<String> WHITE_LIST_URL = List.of(
            "/swagger-ui/**",
            "/api-docs/**",
            "/v3/api-docs/**",
            "/user/signup"
    );


    @Override
    protected void doFilterInternal(
            @NonNull
            HttpServletRequest request,
            @NonNull
            HttpServletResponse response,
            @NonNull
            FilterChain filterChain) throws ServletException, IOException {

        String servletPath = request.getServletPath();
        if(isPathWhiteListed(servletPath)) {
            filterChain.doFilter(request, response);
            return;
        }
        try{
            validateUserRequest(request);
        }catch (Exception e){
            authorizationFailHandler(response, e.getMessage(), e);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void authorizationFailHandler(@NonNull HttpServletResponse response, String message, Exception e) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String invalidTokenRequest = getInvalidTokenResponse(message, e);
        response.getWriter().write(invalidTokenRequest);
    }

    private String getInvalidTokenResponse(String message, Exception e) {
        return String.format("{\"error\": \"%s\"}", message);
    }
    private void validateUserRequest(HttpServletRequest request) throws Exception {
        final String authHeader = request.getHeader("Authorization");
        final String userId;
        log.info("Request url in jwt filter :{}", request.getRequestURL());
        userId = validateTokenAndExtractSubject(authHeader);
        String jwt = authHeader.substring(7);
        String tokenType = jwtService.extractTokenTypeFromToken(jwt);
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userDetails = userRepository.getUserByUserId(userId);
            if (userDetails == null) {
                throw new Exception("User not found");
            }
            String hashedTokenValue = jwtService.getHashedTokenValue(jwt);
            Optional<UserDeviceDetailsEntity> byUserIdAndAccessToken = tokenType.equalsIgnoreCase("ACCESS") ? userDeviceDetailsRepository
                    .findByUserIdAndAccessToken(userDetails.getUserId(), hashedTokenValue) :
                    userDeviceDetailsRepository.findByUserIdAndRefreshToken(userDetails.getUserId(), hashedTokenValue);
            if (byUserIdAndAccessToken.isEmpty()) {
                throw new Exception("Token Invalid");
            }
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private String validateTokenAndExtractSubject(String authHeader) throws Exception {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            final String jwt = authHeader.substring(7);
            try{
                String tokenType = jwtService.extractTokenTypeFromToken(jwt);
                Objects.requireNonNull(tokenType);
                Claims claims = tokenType.equalsIgnoreCase("ACCESS") ? jwtService.extractAllClaims(jwt) : jwtService.extractAllRefreshClaims(jwt);
                return Objects.requireNonNull(claims.getSubject());
            } catch (ExpiredJwtException e) {
                throw new Exception("Token validity expired");
            } catch (Exception e) {
                throw new Exception("Token is not valid");
            }
        }
        throw new Exception("Token is not valid");
    }

    private boolean isPathWhiteListed(String requestPath) {
        return WHITE_LIST_URL.stream()
                .anyMatch(pattern -> pattern.matches(requestPath));
    }

}
