package com.sist.web.restcontroller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.GoogleLoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.security.JwtTokenProvider;
import com.sist.web.service.SocialAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth/social")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SocialAuthRestController {

    private final SocialAuthService socialAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody GoogleLoginRequest request) {

        LoginResponse response = socialAuthService.googleLogin(request);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
}