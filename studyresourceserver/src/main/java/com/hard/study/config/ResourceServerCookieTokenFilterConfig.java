package com.hard.study.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.hard.study.utils.oauth.ResourceServerTokenExtractor;

public class ResourceServerCookieTokenFilterConfig implements Filter, ApplicationEventPublisherAware {
	
	private static ApplicationContext applicationContext;
	private static AuthenticationManager authenticationManager;
	private static AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new OAuth2AuthenticationDetailsSource();
	private static AuthenticationEventPublisher eventPublisher = new NullEventPublisher();
	private static ApplicationEventPublisher applicationEventPublisher;
	private static ResourceServerTokenServices tokenServices;
	
	// set...() 호출했을 때 Parameter 세팅하기 위해 설정
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	public void setResourceServerTokenServices(ResourceServerTokenServices tokenServices) {
		this.tokenServices = tokenServices;
	}
	
	public void setEventPublisher(AuthenticationEventPublisher eventPublisher) {
		ResourceServerCookieTokenFilterConfig.eventPublisher = eventPublisher;
	}
	
	// 서버 올라갈 때
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		
		// TODO Auto-generated method stub
		System.out.println("cookieTokenFilter setApplicationEventPublisher");
		this.applicationEventPublisher = applicationEventPublisher;
		
	}
	
	// 서버가 올라갈 때 미리 세팅되어 있다.
	public static ApplicationEventPublisher getApplicationEventPublisher() {
		return applicationEventPublisher;
	}
	
	// resource 서버 호출 되자마자
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		// TODO Auto-generated method stub
		System.out.println("cookieTokenFilter doFilter");
		
		final HttpServletRequest req = (HttpServletRequest) request;
		final HttpServletResponse res = (HttpServletResponse) response;
		Object filterChainBean = applicationContext.getBean(FilterChainProxy.class);
		
		// resource서버가 호출이 되면 가장먼저 Authorization을 key로 넘어온 토큰을 확인한다.
		if("suminToken".equals(req.getHeader("Authorization").substring(7, 17))) {
			
			try {
				
				ResourceServerTokenExtractor tokenExtractor = new ResourceServerTokenExtractor();
				Authentication authentication = tokenExtractor.extract(req);
				
				// 토큰 추출이 안됐을 때
				if(authentication == null) {
					
					// 인증여부 확인 ( true = 인증됐으면 )
					if(isAuthenticated()) {
						
						System.out.println("Clear Security Context");
						SecurityContextHolder.clearContext();
						
						// 인증 안됐으면 + Header에 제대로 된 토큰이 없을 때
					} else {
						
						System.out.println("Can't Found Token in Request. Will Continue Chain");
						
					}
					
				} else {
					
					if(authentication instanceof AbstractAuthenticationToken) {
						
						AbstractAuthenticationToken needsDetails = (AbstractAuthenticationToken) authentication;
						req.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, authentication.getPrincipal());
						needsDetails.setDetails(authenticationDetailsSource.buildDetails(req));
						
					}
					
					Authentication authResult = authenticationManager.authenticate(authentication);
					
					AuthenticationEventPublisher ePublisher = new DefaultAuthenticationEventPublisher(getApplicationEventPublisher());
					setEventPublisher(ePublisher);
					eventPublisher.publishAuthenticationSuccess(authResult);
					SecurityContextHolder.getContext().setAuthentication(authResult);
					setPostProcessFilter(filterChainBean, filterChainBean.toString(), false);
					chain.doFilter(request, response);
					
				}
				
			} catch(Exception e) {
				
				SecurityContextHolder.clearContext();
				eventPublisher.publishAuthenticationFailure(new BadCredentialsException(e.getMessage(), e),
						new PreAuthenticatedAuthenticationToken("access_token", "N/A"));
				
				e.printStackTrace();
				
			}
			
		} else {
			
			setPostProcessFilter(filterChainBean, filterChainBean.toString(), true);
			chain.doFilter(request, response);
			
		}
		
	}
	
	private Object setPostProcessFilter(Object bean, String beanName, boolean stateless) throws BeansException {
		
		if(bean instanceof FilterChainProxy) {
			
			FilterChainProxy filterChainProxy = (FilterChainProxy) bean;
			
			for(SecurityFilterChain filterChain : filterChainProxy.getFilterChains()) {
				
				for(Filter filter : filterChain.getFilters()) {
					
					if(filter instanceof OAuth2AuthenticationProcessingFilter) {
						
						OAuth2AuthenticationProcessingFilter oauth2Filter = (OAuth2AuthenticationProcessingFilter) filter;
						
						OAuth2AuthenticationManager oauth2Manager = new OAuth2AuthenticationManager();
						oauth2Manager.setTokenServices(tokenServices);
						oauth2Filter.setAuthenticationManager(oauth2Manager);
						
						
						if(!stateless) {
							
							oauth2Filter.setTokenExtractor(new ResourceServerTokenExtractor());
							oauth2Filter.setStateless(stateless);
							
						} else {
							
							oauth2Filter.setTokenExtractor(new BearerTokenExtractor());
							oauth2Filter.setStateless(stateless);
							
						}
						
					}
					
				}
				
			}
			
			
		}
		
		return bean;
		
	}
	
	// 인증 됐는지 여부
	private boolean isAuthenticated() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			
			// 인증 안됨
			return false;
			
		}
		
		return true;
		
	}
	
	private static final class NullEventPublisher implements AuthenticationEventPublisher {
		
		public void publishAuthenticationFailure(AuthenticationException exception, Authentication authentication) {
		}

		public void publishAuthenticationSuccess(Authentication authentication) {
		}
		
	}
	
}
