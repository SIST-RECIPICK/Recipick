package com.sist.web.service;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.LoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.dto.NicknameCheckResponse;
import com.sist.web.dto.PasswordResetLinkRequest;
import com.sist.web.dto.ReissueResponse;
import com.sist.web.dto.SignupRequest;
import com.sist.web.dto.SignupResponse;
import com.sist.web.security.JwtUser;

public interface AuthService {
	// [이메일 중복 검사]
	EmailCheckResponse checkEmailDuplicate(String email);

	// [닉네임 중복 검사]
	NicknameCheckResponse checkNicknameDuplicate(String nickname);

	// [회원가입]
	SignupResponse signup(SignupRequest request);

	// [로그인]
	LoginResponse login(LoginRequest request);

	// [로그아웃]
	void logout(JwtUser jwtUser, String refreshToken, String authHeader);

	// [토큰 재발급]
	ReissueResponse reissue(String refreshToken);

	// [비밀번호 재설정 링크 요청]
	void requestPasswordReset(PasswordResetLinkRequest request);
}
