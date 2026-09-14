package com.sist.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// /uploads/파일명으로 들어오는 웹 요청을 C:/upload/파일명이라는 실제 디스크 경로와 연결해주는 "통로" 역할을 하는 설정
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:///C:/upload/");
	}
}