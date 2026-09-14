package com.sist.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

// [SwaggerUI 설정]
@Configuration
public class SwaggerConfig {

	private static final String BEARER_SCHEME_NAME = "bearerAuth";

	// Swagger UI에 Authorize(자물쇠) 버튼 추가 - Bearer JWT 인증 스킵 등록
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
								.name(BEARER_SCHEME_NAME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
