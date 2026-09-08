package com.sist.web.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

// [Authorization 헤더 파싱 -> 토큰 검증 -> SecurityContext에 인증 정보 등록]
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER = "Authorization";
	private static final String PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HEADER);

		if (header != null && header.startsWith(PREFIX)) {
			String token = header.substring(PREFIX.length());

			try {
				Claims claims = jwtTokenProvider.parseClaims(token);
				int userId = Integer.parseInt(claims.getSubject());
				String role = claims.get("role", String.class);

				JwtUser jwtUser = new JwtUser(userId, role);
				Authentication authentication = new UsernamePasswordAuthenticationToken(
						jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException e) {
				// 토큰 검증 실패(만료/서명오류 등) - 인증 안 된 상태로 다음 필터로 진행
			}
		}

		filterChain.doFilter(request, response);
	}
}
