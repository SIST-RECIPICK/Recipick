package com.sist.web.security;

import java.io.IOException;
import java.util.List;

import org.springframework.data.redis.core.StringRedisTemplate;
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

// [요청 도착 시 가장 먼저 사용자 jwt 검증]
// SecurityConfig에서 .addFilterBefore로 사용됨
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER = "Authorization";
	private static final String PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final StringRedisTemplate redisTemplate;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String header = request.getHeader(HEADER);
        System.out.println("[DEBUG] header=[" + header + "]");

		if (header != null && header.startsWith(PREFIX)) {
			String token = header.substring(PREFIX.length());

			try {
				Claims claims = jwtTokenProvider.parseClaims(token);

				// 이미 로그아웃된(블랙리스트) AccessToken 인지 확인
				if (Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + token))) {
					filterChain.doFilter(request, response);
					return;
				}

				int userId = Integer.parseInt(claims.getSubject());
				String role = claims.get("role", String.class);

				JwtUser jwtUser = new JwtUser(userId, role);
				Authentication authentication = new UsernamePasswordAuthenticationToken(
						jwtUser, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException e) {
                e.printStackTrace();
			}
		}

		filterChain.doFilter(request, response);
	}
}
