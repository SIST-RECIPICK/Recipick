package com.sist.web.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// [메일 발송 - 비밀번호 재설정 안내]
@Component
@RequiredArgsConstructor
public class PasswordResetMailSender {

	private final JavaMailSender javaMailSender;

	@Value("${spring.mail.username}")
	private String fromAddress;

	// [비밀번호 재설정 링크 메일]
	public void sendPasswordResetMail(String to, String resetUrl) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(to);
		message.setSubject("[Recipick] 비밀번호 재설정 안내");
		message.setText("아래 링크를 눌러 비밀번호를 재설정해주세요. (유효기간 30분)\n\n" + resetUrl);
		javaMailSender.send(message);
	}

	// [소셜 가입 계정 안내 메일]
	public void sendSocialAccountGuideMail(String to) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromAddress);
		message.setTo(to);
		message.setSubject("[Recipick] 비밀번호 재설정 안내");
		message.setText("해당 이메일은 Google 로그인으로 가입된 계정입니다. Google 로그인을 이용해주세요.");
		javaMailSender.send(message);
	}
}
