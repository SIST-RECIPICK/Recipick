package com.sist.web.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.sist.web.dto.GoogleLoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.exception.AuthException;
import com.sist.web.mapper.SocialAuthMapper;
import com.sist.web.security.JwtTokenProvider;
import com.sist.web.vo.SocialAccountVO;
import com.sist.web.vo.UsersVO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SocialAuthServiceImpl implements SocialAuthService {

    private static final String PROVIDER_GOOGLE = "GOOGLE";

    private final SocialAuthMapper socialAuthMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public LoginResponse googleLogin(GoogleLoginRequest request) {

        String idTokenString = request.getIdToken();

        if (idTokenString == null || idTokenString.isBlank()) {
            throw new AuthException(
                    "INVALID_TOKEN",
                    "구글 인증 토큰이 유효하지 않습니다.",
                    HttpStatus.BAD_REQUEST
            );
        }

        // 1. Google ID Token 검증
        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        new GsonFactory()
                )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

        GoogleIdToken idToken;

        try {
            idToken = verifier.verify(idTokenString);
        } catch (GeneralSecurityException | IOException e) {
            throw new AuthException(
                    "GOOGLE_VERIFY_FAILED",
                    "구글 토큰 검증 중 오류가 발생했습니다.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (idToken == null) {
            throw new AuthException(
                    "INVALID_GOOGLE_TOKEN",
                    "유효하지 않은 구글 토큰입니다.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // 2. 검증된 Google ID Token에서 사용자 정보 추출
        Payload payload = idToken.getPayload();

        String providerId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        if (providerId == null || providerId.isBlank() || email == null || email.isBlank()) {
            throw new AuthException(
                    "INVALID_GOOGLE_USER_INFO",
                    "구글 사용자 정보를 확인할 수 없습니다.",
                    HttpStatus.UNAUTHORIZED
            );
        }

        // 3. 기존 Google 계정인지 확인
        SocialAccountVO socialAccount =
                socialAuthMapper.findSocialAccount(PROVIDER_GOOGLE, providerId);

        UsersVO user;

        if (socialAccount != null) {

            // 기존 Google 계정
            user = socialAuthMapper.findUserById(socialAccount.getUsers_id());

            if (user == null) {
                throw new AuthException(
                        "USER_NOT_FOUND",
                        "연결된 회원 정보를 찾을 수 없습니다.",
                        HttpStatus.UNAUTHORIZED
                );
            }

        } else {

            // 4. 새로운 Google 계정
            // 기존 users에 동일 이메일이 있는지 확인
            user = socialAuthMapper.findUserByEmail(email);

            if (user != null) {

                // 기존 로컬 계정 또는 다른 계정과 자동 연동하지 않음
                throw new AuthException(
                        "EMAIL_ALREADY_EXISTS",
                        "이미 가입된 이메일입니다. 기존 로그인 방식을 이용해주세요.",
                        HttpStatus.CONFLICT
                );
            }

            // 5. 완전히 새로운 회원 생성
            user = new UsersVO();
            user.setEmail(email);
            user.setNickname(
                    name != null && !name.isBlank()
                            ? name
                            : "user_" + UUID.randomUUID().toString().substring(0, 8)
            );

            socialAuthMapper.insertUser(user);

            // 6. Google 계정과 새 users 계정 연결
            SocialAccountVO newSocialAccount = new SocialAccountVO();
            newSocialAccount.setProvider(PROVIDER_GOOGLE);
            newSocialAccount.setProvider_id(providerId);
            newSocialAccount.setUsers_id(user.getId());

            socialAuthMapper.insertSocialAccount(newSocialAccount);
        }

        // 7. 탈퇴 회원 확인
        if ("WITHDRAWN".equals(user.getStatus())) {
            throw new AuthException(
                    "ACCOUNT_WITHDRAWN",
                    "탈퇴 처리된 계정입니다.",
                    HttpStatus.FORBIDDEN
            );
        }

        // 8. Access Token 생성
        String accessToken =
                jwtTokenProvider.createAccessToken(user.getId(), user.getRole());

        // 9. Refresh Token 생성 및 Redis 저장
        String refreshToken = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
                "refresh:" + refreshToken,
                String.valueOf(user.getId()),
                Duration.ofMillis(
                        jwtTokenProvider.getRefreshTokenExpiration()
                )
        );

        // 10. 로그인 응답 생성
        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setAccountStatus(user.getStatus());
        response.setUserId(user.getId());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        response.setRefreshToken(refreshToken);

        return response;
    }
}