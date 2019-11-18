//package com.hard.study.service.cache;
//
//import org.springframework.security.core.userdetails.UserCache;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.cache.NullUserCache;
//import org.springframework.stereotype.Service;
//import org.springframework.util.Assert;
//
//@Service
//public class UserCacheService implements UserDetailsService {
//	
//	private UserCache userCache = new NullUserCache();
//	private final UserDetailsService delegate;
//	
//	public UserCacheService(UserDetailsService delegate) {
//		
//		this.delegate = delegate;
//		
//	}
//	
//	public UserCache getUserCache() {
//		
//		return userCache;
//		
//	}
//
//	public void setUserCache(UserCache userCache) {
//		
//		this.userCache = userCache;
//		
//	}
//
//	public UserDetails loadUserByUsername(String username) {
//		
//		UserDetails user = userCache.getUserFromCache(username);
//
//		if (user == null) {
//			
//			user = delegate.loadUserByUsername(username);
//			
//		}
//
//		Assert.notNull(user, () -> "UserDetailsService " + delegate
//				+ " returned null for username " + username + ". "
//				+ "This is an interface contract violation");
//		
//		userCache.putUserInCache(user);
//		
//		return user;
//		
//	}
//	
//}
