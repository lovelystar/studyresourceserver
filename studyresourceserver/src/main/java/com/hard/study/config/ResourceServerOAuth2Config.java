package com.hard.study.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
// 리소스 서버에 엑세스 하려면 전체인증 ( access_token )이 필요.
// 모든 요청은 Authorization parameter에 + Bearer access_token 으로 인증.
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerOAuth2Config extends ResourceServerConfigurerAdapter {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private FilterSecurityInterceptor filterSecurityInterceptor;
	
	@Autowired
	private TokenStore jdbcTokenStore;
	
	@Autowired
	private ResourceServerTokenServices tokenService;
	
	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		
		resources
			.tokenStore(jdbcTokenStore)
			.tokenServices(tokenService);
		
	}
	
	// 자원서버의 접근권한을 설정
	// cors, csrf 허용
	@Override
	public void configure(HttpSecurity http) throws Exception {
		
		http
			.headers()
				.frameOptions()
					.disable();
		
		http
			.cors()
			.and()
			.authorizeRequests()
			.requestMatchers(CorsUtils:: isPreFlightRequest)
				.permitAll()
			// antMatchers에 있는 링크는 모두 허용하지만 이외의 요청은 인증이 필요하다.
			.antMatchers("/studyresourceserver/**", "/oauth/*")
				.permitAll()
			.anyRequest()
				.authenticated()
			.and()
				// 필터 활성화
				.addFilterBefore(cookieTokenFilter(), AbstractPreAuthenticatedProcessingFilter.class)
				.addFilterAfter(filterSecurityInterceptor, FilterSecurityInterceptor.class)
			.exceptionHandling()
				.accessDeniedHandler(new OAuth2AccessDeniedHandler());
		
	}
	
	@Bean
	public ResourceServerCookieTokenFilterConfig cookieTokenFilter() {
		
		ResourceServerCookieTokenFilterConfig cookieTokenFilter = new ResourceServerCookieTokenFilterConfig();
		cookieTokenFilter.setApplicationContext(applicationContext);
		
		OAuth2AuthenticationManager oauth2AuthenticationManager = new OAuth2AuthenticationManager();
		oauth2AuthenticationManager.setTokenServices(tokenService);
		
		cookieTokenFilter.setAuthenticationManager(oauth2AuthenticationManager);
		cookieTokenFilter.setResourceServerTokenServices(tokenService);
		
		return cookieTokenFilter;
		
	}
}
