package com.sist.web.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.NicknameCheckResponse;
import com.sist.web.dto.SignupRequest;
import com.sist.web.dto.SignupResponse;
import com.sist.web.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AuthRestController {

	private final AuthService authService;

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
}
