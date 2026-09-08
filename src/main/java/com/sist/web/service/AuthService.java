package com.sist.web.service;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.NicknameCheckResponse;

public interface AuthService {
	// [이메일 중복 검사]
	EmailCheckResponse checkEmailDuplicate(String email);

	// [닉네임 중복 검사]
	NicknameCheckResponse checkNicknameDuplicate(String nickname);
}
