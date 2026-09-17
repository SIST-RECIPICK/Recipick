package com.sist.web.service;

import com.sist.web.dto.GoogleLoginRequest;
import com.sist.web.dto.LoginResponse;

public interface SocialAuthService {
	LoginResponse googleLogin(GoogleLoginRequest request);
}