package com.hard.study.service.oauth;

import org.springframework.security.core.Authentication;

public interface AuthenticationService {
	
	public Authentication getAuthentication();
	
}
