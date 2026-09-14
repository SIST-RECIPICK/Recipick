package com.sist.web.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

// [JWT 생성/검증]
@Getter
@Component
public class JwtTokenProvider {

	private final SecretKey key;
	private final long accessTokenExpiration;
	private final long refreshTokenExpiration;

	public JwtTokenProvider(
			@Value("${jwt.secret}") String secret,
			@Value("${jwt.access-token-expiration}") long accessTokenExpiration,
			@Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpiration = accessTokenExpiration;
		this.refreshTokenExpiration = refreshTokenExpiration;
	}

	// [Access Token 발급]
	public String createAccessToken(int userId, String role) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + accessTokenExpiration);

		return Jwts.builder()
				.subject(String.valueOf(userId))
				.claim("role", role)
				.issuedAt(now)
				.expiration(expiry)
				.signWith(key)
				.compact();
	}

	// [토큰 검증 + Claims 추출] - 만료/서명 오류 시 io.jsonwebtoken.JwtException 발생
	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	// [AccessToken 남은 유효시간(ms) 계산] - 블랙리스트 등록 TTL 등록에 사용
	public long getRemainingExpiration(String token) {
        // 원래 만료 시간
		Date expiration = parseClaims(token).getExpiration();
        // 원래 만료 시간 - 지난 시간 = 남은 시간 (이 시간 동안 블랙리스트 등록)
		return expiration.getTime() - System.currentTimeMillis();
	}
}
