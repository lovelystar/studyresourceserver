//package com.hard.study.config;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cache.concurrent.ConcurrentMapCache;
//import org.springframework.context.ApplicationListener;
//import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
//import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
//import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
//import org.springframework.security.oauth2.provider.OAuth2Authentication;
//import org.springframework.stereotype.Component;
//
//import com.hard.study.service.cache.UserCacheService;
//import com.hard.study.service.oauth.AuthenticationService;
//import com.hard.study.vo.oauth.UserInfoVo;
//
//// AbstractAuthenticationEvent 이벤트가 발생했을 때 이벤트가 수신된다.
//// 동작원리 파악하기
//@Component
//public class ResourceServerEventConfig implements ApplicationListener<AbstractAuthenticationEvent>{
//	
//	@Autowired
//	private AuthenticationService authenticationService;
//	
//	@Autowired
//	private UserCacheService userCacheService;
//	
//	@Override
//	public void onApplicationEvent(AbstractAuthenticationEvent event) {
//		
//		if(event instanceof AuthenticationSuccessEvent) {
//			try {
//				
//				Authentication authentication = event.getAuthentication();
//				Authentication getOAuth = authenticationService.getAuthentication();
//				
//				if(authentication != getOAuth) {
//					
//					if(authentication instanceof OAuth2Authentication) {
//						
//						UserDetails userDetail = new UserInfoVo();
//						UserDetails basedUserDetailCache = new UserInfoVo();
//						
//						OAuth2Authentication oauth2 = (OAuth2Authentication) authentication;
//						userDetail = (UserDetails) oauth2.getPrincipal();
//						
//						SpringCacheBasedUserCache basedUserCache = new SpringCacheBasedUserCache(new ConcurrentMapCache("UserDetails"));
//						basedUserDetailCache = userCacheService.loadUserByUsername(userDetail.getUsername());
//						SecurityContextHolder.getContext().setAuthentication(authentication);		            
//						
//						if(basedUserDetailCache != null || basedUserDetailCache.getUsername().equals(""))
//						if(userDetail.getUsername().hashCode() != basedUserDetailCache.getUsername().hashCode()) {
//							
//							if(basedUserDetailCache != null) {
//								
//								basedUserCache.removeUserFromCache(userDetail.getUsername());
//								
//							}
//							
//							basedUserCache.putUserInCache(userDetail);
//							userCacheService.setUserCache(basedUserCache);
//							
//						}
//						
//						System.out.println("AbstractAuthenticationEvent Cache Save Success End");
//						
//					} else {
//						
//						System.out.println("User authenticated by a non OAuth2 mechanism. Class is " + authentication.getClass());
//						
//					}
//					
//				}
//				
//				// authentication == getOAuth
//				System.out.println("AbstractAuthenticationEvent Success Event");
//				
//			} catch(Exception e) {
//				
//				System.out.println("spring Cache Based User Cache Error : " + e);
//				
//			}
//			
//		} else if(event instanceof InteractiveAuthenticationSuccessEvent) {
//			
//			System.out.println("InteractiveAuthenticationSuccessEvent : " + InteractiveAuthenticationSuccessEvent.class);
//			
//		}
//		
//	}
//
//}
