package com.encrypt.encryption_service.controller;

import com.encrypt.encryption_service.dto.request.UserOffBoardRequest;
import com.encrypt.encryption_service.dto.request.UserOnboardRequest;
import com.encrypt.encryption_service.dto.response.AppResponse;
import com.encrypt.encryption_service.dto.response.UserDetailsResponse;
import com.encrypt.encryption_service.dto.response.UserOffBoardResponse;
import com.encrypt.encryption_service.dto.response.UserOnboardResponse;
import com.encrypt.encryption_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AppResponse<UserOnboardResponse>> userOnboard(@RequestBody UserOnboardRequest userOnboardRequest) throws Exception {
        AppResponse<UserOnboardResponse> appResponse = AppResponse.<UserOnboardResponse>builder()
                .data(userService.signIn(userOnboardRequest))
                .status(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(appResponse, HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<AppResponse<UserOffBoardResponse>> userOffboarded(@RequestBody UserOffBoardRequest userOffBoardRequest) throws UsernameNotFoundException {
        AppResponse<UserOffBoardResponse> appResponse = AppResponse.<UserOffBoardResponse>builder()
                .data(userService.signOff(userOffBoardRequest))
                .status(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(appResponse, HttpStatus.OK);
    }

    @GetMapping("/")
    public ResponseEntity<AppResponse<List<UserDetailsResponse>>> getAllUser(){
        AppResponse<List<UserDetailsResponse>> appResponse = AppResponse.<List<UserDetailsResponse>>builder()
                .data(userService.getAllUsers())
                .status(HttpStatus.OK.value())
                .build();

        return new ResponseEntity<>(appResponse, HttpStatus.OK);
    }


    @PostMapping(value = "/token/validate", consumes = "application/json")
    public ResponseEntity<AppResponse<Boolean>> validateToken(@RequestBody Map<String, Object> data) {
        AppResponse<Boolean> venuResponse = AppResponse.<Boolean>builder().status(HttpStatus.OK.value()).data(userService.validateToken(data.get("token").toString())).build();
        return new ResponseEntity<>(venuResponse, HttpStatus.OK);
    }
}
