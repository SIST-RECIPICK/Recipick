package com.sist.web.service;

import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.LoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.dto.NicknameCheckResponse;
import com.sist.web.dto.PasswordResetLinkRequest;
import com.sist.web.dto.PasswordResetValidateResponse;
import com.sist.web.dto.ReissueResponse;
import com.sist.web.dto.SignupRequest;
import com.sist.web.dto.SignupResponse;
import com.sist.web.exception.AuthException;
import com.sist.web.mapper.AuthMapper;
import com.sist.web.security.JwtTokenProvider;
import com.sist.web.security.JwtUser;
import com.sist.web.util.PasswordResetMailSender;
import com.sist.web.vo.LocalAccountVO;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
	private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]{2,10}$");
	private static final Pattern PASSWORD_PATTERN = Pattern
			.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]).{8,20}$");
	private static final String ACCOUNT_STATUS_WITHDRAWN = "WITHDRAWN";
	private static final Duration RECOVERY_TOKEN_TTL = Duration.ofMinutes(5);
	private static final Duration PASSWORD_RESET_TOKEN_TTL = Duration.ofMinutes(30);
	private static final Duration PASSWORD_RESET_REQUEST_LIMIT_TTL = Duration.ofSeconds(60);

	private final AuthMapper authMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final StringRedisTemplate redisTemplate;
	private final PasswordResetMailSender mailSender;

	@Value("${app.password-reset-url}")
	private String passwordResetUrl;

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

	// [회원가입]
	@Override
	@Transactional
	public SignupResponse signup(SignupRequest request) {
		String email = request.getEmail();
		String password = request.getPassword();
		String nickname = request.getNickname();

		// 1. 이메일 형식 검증
		if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
			throw new AuthException("INVALID_EMAIL_FORMAT", "올바른 이메일 형식을 입력해주세요.");
		}

		// 2. 비밀번호 형식 검증
		if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
			throw new AuthException("INVALID_PASSWORD_FORMAT", "비밀번호는 문자, 숫자, 특수기호를 모두 포함해 8~20자로 입력해주세요.");
		}

		// 3. 비밀번호 / 비밀번호 확인 일치 검사
		if (!password.equals(request.getPasswordConfirm())) {
			throw new AuthException("PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다.");
		}

		// 4. 닉네임 형식 검증
		if (nickname == null || !NICKNAME_PATTERN.matcher(nickname).matches()) {
			throw new AuthException("INVALID_NICKNAME_FORMAT", "닉네임은 2~10자의 한글, 영문, 숫자만 사용 가능합니다.");
		}

		// 5. 서버 재검증 - 이메일 중복 (예외: 409)
		if (authMapper.findUserByEmail(email) != null) {
			throw new AuthException("EMAIL_DUPLICATE", "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT);
		}

		// 6. 서버 재검증 - 닉네임 중복 (예외: 409)
		if (authMapper.findUserByNickname(nickname) != null) {
			throw new AuthException("NICKNAME_DUPLICATE", "이미 사용 중인 닉네임입니다.", HttpStatus.CONFLICT);
		}

		// 7. 비밀번호 암호화
		String encodedPassword = passwordEncoder.encode(password);

		// 8. users 레코드 생성 (데이터 DB에 추가)
		UsersVO user = new UsersVO();
		user.setEmail(email);
		user.setNickname(nickname);
		authMapper.insertUser(user);

		// 9. local_accounts 레코드 생성 (데이터 DB에 추가)
		LocalAccountVO localAccount = new LocalAccountVO();
		localAccount.setPassword(encodedPassword);
		localAccount.setUser_id(user.getId());
		authMapper.insertLocalAccount(localAccount);

		// 10. 응답 반환
		SignupResponse response = new SignupResponse();
		response.setUserId(user.getId());
		response.setEmail(user.getEmail());
		response.setNickname(user.getNickname());
		return response;
	}

	// [로그인]
	@Override
	public LoginResponse login(LoginRequest request) {
		String email = request.getEmail();
		String password = request.getPassword();

		// 1. 이메일로 사용자 조회
		UsersVO user = authMapper.findUserByEmail(email);
		if (user == null) {
			throw new AuthException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
		}

		// 2. 로컬 계정 조회 (없으면 소셜 전용 계정)
		LocalAccountVO localAccount = authMapper.findLocalAccountWithPasswordByUserId(user.getId());
		if (localAccount == null) {
			throw new AuthException("SOCIAL_ACCOUNT_ONLY", "구글 로그인을 이용해주세요.", HttpStatus.FORBIDDEN);
		}

		// 3. 비밀번호 검증 (계정 존재 여부 미노출 위해 1번과 동일 에러코드/메시지)
		if (!passwordEncoder.matches(password, localAccount.getPassword())) {
			throw new AuthException("INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
		}

		LoginResponse response = new LoginResponse();
		response.setAccountStatus(user.getStatus());

		// 4. 소프트탈퇴 계정 - 토큰 미발급, 복구용 단기 토큰만 발급
		if (ACCOUNT_STATUS_WITHDRAWN.equals(user.getStatus())) {
			String recoveryToken = UUID.randomUUID().toString();
			redisTemplate.opsForValue().set("recovery:" + recoveryToken, String.valueOf(user.getId()), RECOVERY_TOKEN_TTL);

			response.setRecoveryToken(recoveryToken);
			response.setMessage("탈퇴한 계정입니다. 계정을 복구하시겠습니까?");
			return response;
		}

		// 5. 활성 계정 - Access Token(body) + Refresh Token(Redis 저장 + 쿠키) 발급
		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole());
		String refreshToken = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set("refresh:" + refreshToken, String.valueOf(user.getId()),
				Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()));

		response.setAccessToken(accessToken);
		response.setUserId(user.getId());
		response.setNickname(user.getNickname());
		response.setRole(user.getRole());
		response.setRefreshToken(refreshToken);
		return response;
	}

	// [로그아웃]
	@Override
	public void logout(JwtUser jwtUser, String refreshToken, String authHeader) {
		// 1. 인증 여부 확인
		if (jwtUser == null) {
			throw new AuthException("UNAUTHORIZED", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
		}

		// 2. Refresh Token 삭제
		if (refreshToken != null) {
			redisTemplate.delete("refresh:" + refreshToken);
		}

		// 3. Access Token 블랙리스트 등록 (남은 유효기간만큼 TTL 설정)
		String accessToken = authHeader.substring("Bearer ".length());
		long remainingExpiration = jwtTokenProvider.getRemainingExpiration(accessToken);
		redisTemplate.opsForValue().set("blacklist:" + accessToken, "true", Duration.ofMillis(remainingExpiration));
	}

	// [토큰 재발급]
	@Override
	public ReissueResponse reissue(String refreshToken) {
		// 1. 쿠키 자체가 없음
		if (refreshToken == null) {
			throw new AuthException("INVALID_REFRESH_TOKEN", "세션이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED);
		}

		// 2. Redis에서 Refresh Token 조회 (조회 결과가 곧 유효성 검증)
		String userIdValue = redisTemplate.opsForValue().get("refresh:" + refreshToken);
		if (userIdValue == null) {
			throw new AuthException("INVALID_REFRESH_TOKEN", "세션이 만료되었습니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED);
		}
		int userId = Integer.parseInt(userIdValue);

		// 3. 계정 상태 재조회 (Refresh Token 발급 이후 소프트탈퇴됐을 수 있음)
		UsersVO user = authMapper.findUserStatusById(userId);
		if (ACCOUNT_STATUS_WITHDRAWN.equals(user.getStatus())) {
			redisTemplate.delete("refresh:" + refreshToken);
			throw new AuthException("ACCOUNT_WITHDRAWN", "탈퇴한 계정입니다. 다시 로그인해주세요.", HttpStatus.UNAUTHORIZED);
		}

		// 4. 기존 Refresh Token 삭제 (Rotation)
		redisTemplate.delete("refresh:" + refreshToken);

		// 5. 신규 Access Token 발급
		String newAccessToken = jwtTokenProvider.createAccessToken(userId, user.getRole());

		// 6. 신규 Refresh Token 발급 (Rolling 방식 - TTL 14일로 재설정)
		String newRefreshToken = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set("refresh:" + newRefreshToken, String.valueOf(userId),
				Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()));

		ReissueResponse response = new ReissueResponse();
		response.setAccessToken(newAccessToken);
		response.setRefreshToken(newRefreshToken);
		return response;
	}

	// [비밀번호 재설정 링크 요청]
	@Override
	public void requestPasswordReset(PasswordResetLinkRequest request) {
		String email = request.getEmail();

		// 1. 이메일 형식 검증
		if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
			throw new AuthException("INVALID_EMAIL_FORMAT", "올바른 이메일 형식을 입력해주세요.");
		}

		// 2. 요청 주기 제한 (계정 존재 여부 확인 이전에 설정 - 존재 유무 비노출)
		boolean firstRequest = Boolean.TRUE.equals(redisTemplate.opsForValue()
				.setIfAbsent("pwResetLimit:" + email, "1", PASSWORD_RESET_REQUEST_LIMIT_TTL));
		if (!firstRequest) {
			throw new AuthException("TOO_MANY_REQUESTS", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", HttpStatus.TOO_MANY_REQUESTS);
		}

		// 3. 가입 여부 확인
		UsersVO user = authMapper.findUserByEmail(email);
		if (user == null) {
			// 미가입 - 토큰 생성/메일 발송 없이 동일한 성공 응답
			return;
		}

		// 4. 로컬/소셜 가입 여부 확인 후 메일 발송
		try {
			LocalAccountVO localAccount = authMapper.findLocalAccountByUserId(user.getId());
			if (localAccount == null) {
				mailSender.sendSocialAccountGuideMail(email);
			} else {
				String token = rotatePasswordResetToken(user.getId());
				mailSender.sendPasswordResetMail(email, passwordResetUrl + "?token=" + token);
			}
		} catch (MailException e) {
			log.error("비밀번호 재설정 메일 발송 실패", e);
			throw new AuthException("MAIL_SEND_FAILED", "일시적인 오류로 메일 전송에 실패했습니다. 잠시 후 다시 시도해주세요.",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// [비밀번호 재설정 링크 유효성 검증]
	@Override
	public PasswordResetValidateResponse validatePasswordResetToken(String token) {
		// 1. token 파라미터 누락
		if (token == null || token.isBlank()) {
			throw new AuthException("MISSING_TOKEN", "잘못된 접근입니다.", HttpStatus.BAD_REQUEST);
		}

		PasswordResetValidateResponse response = new PasswordResetValidateResponse();

		// 2. Redis pwReset:{token} 조회 (삭제하지 않음)
		String userIdValue = redisTemplate.opsForValue().get("pwReset:" + token);
		if (userIdValue == null) {
			response.setValid(false);
			return response;
		}

		// 3. 계정 상태 재확인 (ACTIVE일 때만 valid: true)
		UsersVO user = authMapper.findUserStatusById(Integer.parseInt(userIdValue));
		response.setValid(user != null && "ACTIVE".equals(user.getStatus()));
		return response;
	}

	// [기존 활성 토큰 무효화 후 신규 비밀번호 재설정 토큰 발급]
	private String rotatePasswordResetToken(int userId) {
		String oldToken = redisTemplate.opsForValue().get("pwResetUser:" + userId);
		if (oldToken != null) {
			redisTemplate.delete("pwReset:" + oldToken);
		}

		String token = UUID.randomUUID().toString();
		redisTemplate.opsForValue().set("pwReset:" + token, String.valueOf(userId), PASSWORD_RESET_TOKEN_TTL);
		redisTemplate.opsForValue().set("pwResetUser:" + userId, token, PASSWORD_RESET_TOKEN_TTL);
		return token;
	}
}
