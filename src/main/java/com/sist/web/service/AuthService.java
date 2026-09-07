package com.sist.web.service;

import com.sist.web.dto.EmailCheckResponse;

public interface AuthService {
	EmailCheckResponse checkEmailDuplicate(String email);
}
