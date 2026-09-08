package com.sist.web.service;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.LoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.dto.NicknameCheckResponse;
import com.sist.web.dto.SignupRequest;
import com.sist.web.dto.SignupResponse;

public interface AuthService {
	// [이메일 중복 검사]
	EmailCheckResponse checkEmailDuplicate(String email);

	// [닉네임 중복 검사]
	NicknameCheckResponse checkNicknameDuplicate(String nickname);

	// [회원가입]
	SignupResponse signup(SignupRequest request);

	// [로그인]
	LoginResponse login(LoginRequest request);
}
