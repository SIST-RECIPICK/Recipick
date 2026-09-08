package com.sist.web.restcontroller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.LoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.dto.NicknameCheckResponse;
import com.sist.web.dto.SignupRequest;
import com.sist.web.dto.SignupResponse;
import com.sist.web.security.JwtTokenProvider;
import com.sist.web.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthRestController {

	private final AuthService authService;
	private final JwtTokenProvider jwtTokenProvider;

	// [이메일 중복 검사]
	@GetMapping("/email/check")
	public EmailCheckResponse checkEmail(@RequestParam("email") String email) {
		return authService.checkEmailDuplicate(email);
	}

	// [닉네임 중복 검사]
	@GetMapping("/nickname/check")
	public NicknameCheckResponse checkNickname(@RequestParam("nickname") String nickname) {
		return authService.checkNicknameDuplicate(nickname);
	}

	// [회원가입]
	@PostMapping("/signup")
	public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(request));
	}

	// [로그인]
	@PostMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
		LoginResponse response = authService.login(request);

		// 소프트탈퇴(WITHDRAWN) - 토큰 미발급이라 쿠키 없이 그대로 반환
		if (response.getRefreshToken() == null) {
			return ResponseEntity.ok(response);
		}

		ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
				.httpOnly(true)
				.secure(true)
				.path("/")
				.sameSite("Strict")
				.maxAge(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()))
				.build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
	}
}
