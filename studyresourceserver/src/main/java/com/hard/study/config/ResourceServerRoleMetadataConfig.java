package com.hard.study.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import com.hard.study.service.oauth.AuthenticationService;
import com.hard.study.service.oauth.RoleHierarchyService;
import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;
import com.hard.study.vo.oauth.UserInfoVo;

// URL의 Role 정보를 가져오는 부분
// Bean과 Component는 둘다 Bean만들때 사용.
// 차이
// Bean : 개발자가 직접 제어 불가능 + 개발자가 작성한 Method를 통해 반환되는 객체를 Bean으로 등록
// Component : 개발자가 직접 작성한 class를 Bean으로 등록
@Component
public class ResourceServerRoleMetadataConfig implements FilterInvocationSecurityMetadataSource {
	
	private static final String AUTHENTICATED_FULLY = "IS_AUTHENTICATED_FULLY";
	private static final String AUTHENTICATED_REMEMBERED = "IS_AUTHENTICATED_REMEMBERED";
	private static final String AUTHENTICATED_ANONYMOUSLY = "IS_AUTHENTICATED_ANONYMOUSLY";
	private static final String scope_Prefix = "SCOPE_";
	
	private HashMap<String, Collection<ConfigAttribute>> requestMap = null;
	private Map<String, List<OAuthResourceAuthorityVo>> requestHttpMap = null;
	
	@Autowired
	private RoleHierarchyService roleHierarchyService;
	
	@Autowired
	private AuthenticationService authenticationService;
	
	// URL별 권한 동적 처리
	// 참고 : Spring Security가 가로챈 모든 요청은 getAttributes가 호출되므로 캐싱이 필요
	@Override
	public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
		FilterInvocation fi = (FilterInvocation) object;
		String url = fi.getRequestUrl();
		String httpMethod = fi.getRequest().getMethod();
		Collection<ConfigAttribute> configAttributes = new ArrayList<>();
		
		HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
		AntPathRequestMatcher matcher;
		
		try {
			
			if(url != null) {
				
				if(requestMap == null || requestMap.isEmpty()) {
					
					loadResource();
					
				}
				
				// Map을 반복시키는 방법
				// 1. entrySet 이용
				// Map<String, String> map = new HashMap<>();
				// for(Map.Entry<String, String> em : map.entrySet())
				// { System.out.println("key = " + em.getKey() + ", value = " + em.getValue()); }
				
				// 2. keySet 이용
				// Map<String, String> map = new HashMap<>();
				// for(String key : map.keySet())
				// { System.out.println("key = " + key + ", value = " + map.get(key)); }
				
				// 3. iterator 이용
				// Map<String, String> map = new HashMap<>();
				// Iterator<String> keys = map.keyset().iterator();
				// while(keys.hasNext()){
				// String key = keys.next();
				// System.out.println("key = " + key + ", value = " + map.get(key));}
				for(Map.Entry<String, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
					
					int rowSize = requestHttpMap.get(entry.getKey()).size(); // List<OAuthResourceAuthorityVo>의 크기
					
					for(int i=0; i<rowSize; i++) {
						
						OAuthResourceAuthorityVo securityResources = requestHttpMap.get(entry.getKey()).get(i);
						
						// securityResources가 null이 아니고, resource_id가 null이 아니고, httpmethod가 맞을 때
						if(null != securityResources && null != securityResources.getResourceId() && securityResources.getHttpMethod().toUpperCase().toString().equals(httpMethod.toUpperCase().toString())) {
							
							matcher = new AntPathRequestMatcher(securityResources.getResourcePattern(), securityResources.getHttpMethod());
							
						} else {
							
							// 맞지 않을 때나 null일 때
							matcher = new AntPathRequestMatcher(entry.getKey());
							
						}
						
						if(matcher.matches(request) && entry.getValue().size() > 0) {
							
							// 람다식 사용
							// 
							configAttributes = entry.getValue().stream().map(in -> new DynamicConfigAttribute(in.getAttribute())).collect(Collectors.toList());
							
							if(configAttributes.size() >= 1) {
								
								requestMap.clear();
								requestHttpMap.clear();
								
							}
							
							return configAttributes;
							
						}
						
					}
					
				}
				
			}
			
			throw new IllegalArgumentException();
			
		} catch(Exception e) {
			
			throw new AccessDeniedException("Not Matched User Authority : ", e);
			
		}
		
	}
	
	public void loadResource() throws Exception {
		
		requestMap = new HashMap<String, Collection<ConfigAttribute>>();
		requestHttpMap = new HashMap<String, List<OAuthResourceAuthorityVo>>();
		
		ConfigAttribute configAttribute;
		Collection<ConfigAttribute> configAttributeArray;
		
		UserInfoVo userInfoVo = new UserInfoVo();
		UserDetails userDetails = new UserInfoVo();
		
		Authentication auth = authenticationService.getAuthentication();
		OAuth2Authentication oauth2 = (OAuth2Authentication) auth;
		
		userDetails = (UserDetails) oauth2.getPrincipal();
		// 전달된 인증이 정의된 클래스의 인스턴스가 맞는지에 따라 신뢰결정을 내린다.
		AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
		Set<String> scope = oauth2.getOAuth2Request().getScope();
		
		List<OAuthResourceAuthorityVo> resourceAuthorityVoList = new ArrayList<>();
		OAuthResourceAuthorityVo resourceAuthorityVo = new OAuthResourceAuthorityVo();
		resourceAuthorityVo.setUserName(userDetails.getUsername());
		resourceAuthorityVoList = roleHierarchyService.getUserResource(resourceAuthorityVo);
		userInfoVo.setResourceAuthorityList(resourceAuthorityVoList);
		
		// 인증이 됐다면
		if(userDetails != null) {
			
			for(OAuthResourceAuthorityVo resourceUrl : userInfoVo.getResourceAuthorityList()) {
				
				configAttributeArray = new ArrayList<>();
				
				for(GrantedAuthority authority : userDetails.getAuthorities()) {
					
					if(0 == configAttributeArray.size()) {
						
						// authenticationTrustResolver는 항상 false
						
						// 익명의 사용자가 아니고, remember me 사용자가 아닐 때 AUTHENTICATED_FULLY
						if(!authenticationTrustResolver.isAnonymous(oauth2) && !authenticationTrustResolver.isRememberMe(oauth2)) {
							
							configAttribute = new SecurityConfig(AUTHENTICATED_FULLY);
							configAttributeArray.add(configAttribute);
							
							// 익명의 사용자가 아니고, remember me 사용자일 때 AUTHENTICATED_REMEMBERED
						} else if(authenticationTrustResolver.isRememberMe(oauth2) && !authenticationTrustResolver.isAnonymous(oauth2)) {
							
							configAttribute = new SecurityConfig(AUTHENTICATED_REMEMBERED);
							configAttributeArray.add(configAttribute);
							
							// 익명의 사용자 이고, remember me 사용자가 아닐 때 AUTHENTICATED_ANONYMOUSLY
						} else if(authenticationTrustResolver.isAnonymous(oauth2) && !authenticationTrustResolver.isRememberMe(oauth2)) {
							
							configAttribute = new SecurityConfig(AUTHENTICATED_ANONYMOUSLY);
							configAttributeArray.add(configAttribute);
							
						}
						
						for(int i=0; i<scope.size(); i++) {
							
							configAttribute = new SecurityConfig(scope_Prefix.toUpperCase() + scope.toArray()[i].toString().toUpperCase());
							configAttributeArray.add(configAttribute);
							
						}
						
					}
					
					configAttribute = new SecurityConfig(authority.getAuthority());
					configAttributeArray.add(configAttribute);
					
				}
				
				requestMap.put(resourceUrl.getResourceId(), configAttributeArray);
				configAttributeArray = new ArrayList<>();
				requestHttpMap.put(resourceUrl.getResourceId(), userInfoVo.getResourceAuthorityList());
				
			}
			
		}
		
	}
	
	public class DynamicConfigAttribute implements ConfigAttribute {
		
		private static final long serialVersionUID = 1L;
		private String attribute;
		
		public DynamicConfigAttribute(String attribute) {
			this.attribute = attribute;
		}

		@Override
		public String getAttribute() {
			
			// Possible values to getAttribute's return
			// 
			// IS_AUTHENTICATED_ANONYMOUSLY, IS_AUTHENTICATED_REMEMBERED
			// IS_AUTHENTICATED_FULLY, SCOPE_<scope>, ROLE_<role>
			
			return this.attribute;
			
		}
		
		@Override
		public String toString() {
			
			return this.attribute;
			
		}
		
	}

	@Override
	public Collection<ConfigAttribute> getAllConfigAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// return false에서 변경
	// isAssignableFrom는 instanceof와 비슷
	// 현재 만든 클래스가 FilterInvocation 인스턴스 타입과 맞는지 다른지
	// 아래는 true
	@Override
	public boolean supports(Class<?> clazz) {
		// TODO Auto-generated method stub
		return FilterInvocation.class.isAssignableFrom(clazz);
	}

}
