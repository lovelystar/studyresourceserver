package com.hard.study.service.oauth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationServiceImpl implements AuthenticationService {
	
	@Override
	public Authentication getAuthentication() {
		
		return SecurityContextHolder.getContext().getAuthentication();
		
	}
	
}
