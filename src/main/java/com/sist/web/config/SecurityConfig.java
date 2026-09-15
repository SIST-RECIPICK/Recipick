package com.sist.web.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.sist.web.security.JwtAuthenticationFilter;
import com.sist.web.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtTokenProvider jwtTokenProvider;
	private final StringRedisTemplate redisTemplate;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		http
		.cors(cors -> cors.configurationSource(corsConfigurationSource()))
			.csrf(csrf -> csrf.disable()) //위조 방지
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.formLogin(form -> form.disable())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					//========= 모든 사용자 ========//
					"/login", "/signup"
				).permitAll()
			    .requestMatchers(
			    	//========= ADMIN 사용자 ========//
			    	"/admin/**"
			    ).hasRole("ADMIN")
			    .requestMatchers(
			    	//========== USER와 ADMIN 사용자 ==========//
			    	"/calendar/**", 				//식단관리 캘린더
			    	"/chat/**",						//채팅문의
			    	"/recipe/interaction", 			//레시피 상세보기 북마크, 좋아요
			    	"/recipe/my-list",   			//레시피 북마크, 좋아요 리스트
			    	"/recipe/like",					//좋아요 토글버튼
			    	"/recipe/mylist",				//마이페이지 - 나의 레시피 목록
			    	"/recipe/insert",				//레시피 등록
			    	"/recipe/delete",				//레시피 삭제
			    	"/recipe/update",				//레시치 수정
			    	"/recipe/search",				//식단관리 레시피 검색
			    	"/refrige/**"					//냉장고 파먹기 			    	
			    ).hasAnyRole("USER", "ADMIN") 
				.anyRequest().permitAll() //나머지는 회원 비회원 모두 허용
			)
			.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, redisTemplate), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	 @Bean
	    public CorsConfigurationSource corsConfigurationSource() {

	        CorsConfiguration configuration = new CorsConfiguration();

	        configuration.setAllowedOriginPatterns(List.of("*")); //모든 Origin을 허용
	        configuration.setAllowedMethods(
	            List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
	        );
	        configuration.setAllowedHeaders(List.of("*"));
	        configuration.setAllowCredentials(true);

	        UrlBasedCorsConfigurationSource source =
	                new UrlBasedCorsConfigurationSource();

	        source.registerCorsConfiguration("/**", configuration);

	        return source;
	    }

}
