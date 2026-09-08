package com.sist.web.service;

import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.NicknameCheckResponse;
import com.sist.web.exception.AuthException;
import com.sist.web.mapper.AuthMapper;
import com.sist.web.vo.LocalAccountVO;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");

	private final AuthMapper authMapper;

	// [이메일 중복 검사]
	@Override
	public EmailCheckResponse checkEmailDuplicate(String email) {
		// 1. 이메일 형식 검증
		if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
			throw new AuthException("INVALID_EMAIL_FORMAT", "올바른 이메일 형식을 입력해주세요.");
		}

		// 2. 해당 이메일 갖고 있는 사용자 조회 (중복 사용자 확인)
		UsersVO user = authMapper.findUserByEmail(email);

		EmailCheckResponse response = new EmailCheckResponse();

		// [응답] 아무도 사용하지 않는 메일이라면 -> 사용 가능
		if (user == null) {
			response.setAvailable(true);
			response.setReason(null);
			return response;
		}

		// 3. 메일 사용중인 사용자가 있다면 로컬 사용자인지, 소셜 사용자인지 확인
		LocalAccountVO localAccount = authMapper.findLocalAccountByUserId(user.getId());

		// [응답] 사용 불가 (중복 or 소셜 계정인지 응답)
		response.setAvailable(false);
		response.setReason(localAccount != null ? "DUPLICATE" : "SOCIAL_ONLY");
		return response;
	}

	// [닉네임 중복 검사]
	@Override
	public NicknameCheckResponse checkNicknameDuplicate(String nickname) {
		// 1. 닉네임 형식 검증
		if (nickname == null || !NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new AuthException("INVALID_NICKNAME_FORMAT", "닉네임은 2~10자의 한글, 영문, 숫자만 사용 가능합니다.");
		}

		// 2. 해당 닉네임 갖고 있는 사용자 조회 (중복 사용자 확인)
		UsersVO user = authMapper.findUserByNickname(nickname);

		NicknameCheckResponse response = new NicknameCheckResponse();
		response.setAvailable(user == null);
		return response;
	}
}
