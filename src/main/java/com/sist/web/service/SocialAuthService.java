package com.sist.web.service;

import com.sist.web.dto.GoogleLoginRequest;
import com.sist.web.dto.LoginResponse;
import com.sist.web.dto.SocialLinkRequest;

public interface SocialAuthService {
	LoginResponse googleLogin(GoogleLoginRequest request);
	LoginResponse linkAndLogin(SocialLinkRequest request);
}