package com.sist.web.restcontroller;

import java.time.Duration;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sist.web.dto.EmailCheckResponse;
import com.sist.web.dto.LoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.dto.NicknameCheckResponse;
import com.sist.web.dto.PasswordResetLinkRequest;
import com.sist.web.dto.PasswordResetRequest;
import com.sist.web.dto.PasswordResetValidateResponse;
import com.sist.web.dto.RecoverRequest;
import com.sist.web.dto.ReissueResponse;
import com.sist.web.dto.SignupRequest;
import com.sist.web.dto.SignupResponse;
import com.sist.web.dto.WithdrawRequest;
import com.sist.web.security.JwtTokenProvider;
import com.sist.web.security.JwtUser;
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

	// [로그아웃]
    // 1. 어떤 토큰을 만료시켜야 하는지 알아야 하기 때문에 @CookieValue로 쿠키에서 RefreshToken 가져오기
    // 2. Service: RefreshToken 사용해서 AccessToken 블랙리스트 등록, Redis에 저장된 RefreshToken 제거
    // 3. Controller: 브라우저의 쿠키에 저장되어져 있는 RefreshToken의 잔여 시간 0으로 수정
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(
			@AuthenticationPrincipal JwtUser jwtUser,
			@CookieValue(value = "refreshToken", required = false) String refreshToken,
			// @AuthenticationPrincipal은 파싱된 userId/role만 제공 - 블랙리스트 등록에는 원본 토큰 문자열이 필요해 별도로 받음
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		authService.logout(jwtUser, refreshToken, authHeader);

		ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
				.httpOnly(true)
				.secure(true)
				.path("/")
				.sameSite("Strict")
				.maxAge(0)
				.build();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(Map.of("message", "로그아웃되었습니다."));
	}

	// [토큰 재발급]
	@PostMapping("/reissue")
	public ResponseEntity<ReissueResponse> reissue(
			@CookieValue(value = "refreshToken", required = false) String refreshToken) {
		ReissueResponse response = authService.reissue(refreshToken);

		ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
				.httpOnly(true)
				.secure(true)
				.path("/")
				.sameSite("Strict")
				.maxAge(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()))
				.build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
	}

	// [회원탈퇴(소프트)]
	@PostMapping("/withdraw")
	public ResponseEntity<Map<String, String>> withdraw(
			@AuthenticationPrincipal JwtUser jwtUser,
			@CookieValue(value = "refreshToken", required = false) String refreshToken,
			@RequestHeader(value = "Authorization", required = false) String authHeader,
			@RequestBody(required = false) WithdrawRequest request) {

		String password = request != null ? request.getPassword() : null;
		authService.withdraw(jwtUser, password, refreshToken, authHeader);

		ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
				.httpOnly(true)
				.secure(true)
				.path("/")
				.sameSite("Strict")
				.maxAge(0)
				.build();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(Map.of("message", "탈퇴가 완료되었습니다."));
	}

	// [계정 복구]
	@PostMapping("/recover")
	public ResponseEntity<LoginResponse> recover(@RequestBody RecoverRequest request) {
		LoginResponse response = authService.recover(request.getRecoveryToken());

		ResponseCookie cookie = ResponseCookie.from("refreshToken", response.getRefreshToken())
				.httpOnly(true)
				.secure(true)
				.path("/")
				.sameSite("Strict")
				.maxAge(Duration.ofMillis(jwtTokenProvider.getRefreshTokenExpiration()))
				.build();

		return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString()).body(response);
	}

	// [비밀번호 재설정 링크 요청]
	@PostMapping("/password/reset-request")
	public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody PasswordResetLinkRequest request) {
		authService.requestPasswordReset(request);
		return ResponseEntity.ok(Map.of("message", "입력하신 이메일로 안내 메일을 전송했습니다."));
	}

	// [비밀번호 재설정 링크 유효성 검증]
	@GetMapping("/password/reset/validate")
	public PasswordResetValidateResponse validatePasswordResetToken(
			@RequestParam(value = "token", required = false) String token) {
		return authService.validatePasswordResetToken(token);
	}

	// [비밀번호 재설정]
	@PostMapping("/password/reset")
	public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequest request) {
		authService.resetPassword(request);
		return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
	}
}
