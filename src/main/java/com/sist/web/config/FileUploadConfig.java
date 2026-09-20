package com.sist.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// /uploads/파일명으로 들어오는 웹 요청을 app.upload-dir 경로의 실제 디스크 파일과 연결해주는 "통로" 역할을 하는 설정
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

	@Value("${app.upload-dir}")
	private String uploadDir;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations(toResourceLocation(uploadDir));
	}

	// Windows 드라이브 경로("C:/upload")는 file:///, 리눅스 절대경로("/data/uploads")는 file: 접두어가 필요하므로 구분
	private String toResourceLocation(String path) {
		String normalized = path.replace('\\', '/');
		if (!normalized.endsWith("/")) {
			normalized += "/";
		}
		boolean isWindowsPath = normalized.matches("^[A-Za-z]:/.*");
		return (isWindowsPath ? "file:///" : "file:") + normalized;
	}
}