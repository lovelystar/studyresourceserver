package com.hard.study.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.vote.ScopeVoter;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.util.FileCopyUtils;

@Configuration
@EnableWebSecurity
// ResourceServerOAuth2Config에도 설정되어있는데 두군데에서 사용하려면
// application.properties에 spring.main.allow-bean-definition-overriding=true
// 를 설정해야 한다.
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private DataSource dataSource;
	
	// 접근 권한 설정
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
			.authorizeRequests()
				.anyRequest()
					.authenticated()
			.and()
			// 세션 컨트롤
			.sessionManagement()
				// SessionCreationPolicy.IFREQUIRED : 기본값, 필요한 경우에만 생성
				// SessionCreationPolicy.NEVER : 세션을 만들지 않겠다
				// SessionCreationPolicy.STATELESS : 보안을 위해 세션이 생성되거나 사용되지 않는다
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and()
			.csrf()
				.disable();
		
	}
	
	@Bean
	public TokenStore tokenStore() {
		
		return new JdbcTokenStore(dataSource);
		
	}
	
	@Bean
	// Primary와 Lazy 차이
	// Primary : 서버올릴 때 Bean 생성 + 같은 이름의 Bean이 있다면 Primary가 붙은 Bean으로 생성
	// Lazy : Bean생성 보류하다가 호출되면 Bean 생성
	@Primary
	public DefaultTokenServices tokenServices() {
		
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		
		ResourceServerFilterChainConfig filterChain = applicationContext.getBean(ResourceServerFilterChainConfig.class);
		filterChain.setTokenServices(defaultTokenServices);
		
		return defaultTokenServices;
		
	}
	
	// 복호화
	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		
		byte[] bdata = null;
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		ClassPathResource cpr = new ClassPathResource("/publickey/publicKey.txt");
		
		try {
			
			bdata = FileCopyUtils.copyToByteArray(cpr.getInputStream());
			
		} catch(IOException e) {
			
			e.printStackTrace();
			
		}
		
		String publicKey = new String(bdata, StandardCharsets.UTF_8);
		converter.setVerifierKey(publicKey);
		
		return converter;
		
	}
	
	// UserDetails 객체의 캐시를 제공
	// 구현할 때 매개변수 설정, 만료전에 엔티티를 강제로 제거하는 방법을 제공해야 함.
	// 매번 DB에 Access하여 유효성 검사하기에는 비용이 너무 크기때문에 캐시에 UserDetails정보를 구성
	// 캐싱은 일반적으로 원격 Client나 웹 서비스와 같이 서버 측 상태를 유지하지 않는 응용 프로그램에서 사용
	@Bean
	public UserCache userCache() throws Exception {
		
		return new SpringCacheBasedUserCache(new ConcurrentMapCache("UserDetails"));
		
	}
	
	@Bean
	@Override
	// 인증
	public AuthenticationManager authenticationManager() throws Exception {
		
		AuthenticationManager authenticationManager = super.authenticationManagerBean();
		return authenticationManager;
		
	}
	
	// 요청한 URL의 접근권한 확인
	// URL의 접근권한을 확인하고, 사용자의 ROLE을 이용하여 AccessVote를 진행한다.
	// DB에서 관리할꺼지만 /url 매칭이기 때문에 하드코딩과 비슷...
	@Bean
	public FilterInvocationSecurityMetadataSource filterInvocationSecurityMetadataSource() {
		
		return new ResourceServerRoleMetadataConfig();
		
	}
	
	// 아래 Bean 3개는 결국 GROUP_ADMIN>GROUP_USER>ROLE_USER>..... 같은 권한을 가져오기 위해 추가
	// 즉, AccessDecisionManager Bean에서 투표하려고 만듬.
	@Bean
	public RoleHierarchyVoter roleVoter() {
		
		return new RoleHierarchyVoter(roleHierarchy());
		
	}
	
	@Bean
	public ResourceServerRoleHierarchyConfig resourceServerRoleHierarchyConfig() {
		
		return new ResourceServerRoleHierarchyConfig();
		
	}
	
	@Bean
	public RoleHierarchyImpl roleHierarchy() {
		
		return resourceServerRoleHierarchyConfig().getRoleHierarchyImpl();
		
	}
	
	// 권한을 통해 접근 제어
	// 인증이 완료된 사용자가 리소스에(URL) 접근할 때 해당요청을 허용할 것인지 판단
	// true / false 로 리턴이 되며
	// 최종 검증은 FilterSecurityInterceptor에서 진행
	@Bean
	public AccessDecisionManager accessDecisionManager() {
		
		List<AccessDecisionVoter<? extends Object>> accessDecisionVoter = new ArrayList<>();
		accessDecisionVoter.add(new AuthenticatedVoter()); // 인증된 사용자인지
		accessDecisionVoter.add(new ScopeVoter()); // read, write 권한 있는지
		accessDecisionVoter.add(roleVoter());
		
		return new UnanimousBased(accessDecisionVoter);
		// UnanimousBased = 만장일치만 통과
		// ConsensusBased = 다수결
		// AffirmativeBased = 한표만 받아도 통과
		
	}
	
	// URL에 설정된 Role정보에 따라 접근을 허용할 것인지 말 것인지 판단하는 기능을 넣기 위해서 추가
	// oauth_resource_authority테이블의 authority컬럼 확인
	@Bean
	public FilterSecurityInterceptor filterSecurityInterceptor() throws Exception {
		
		FilterSecurityInterceptor filterSecurityInterceptor = new FilterSecurityInterceptor();
		
		// 인증을 먼저 처리한 후 자원에 대한 접근 여부를 판단한다.
		filterSecurityInterceptor.setAuthenticationManager(authenticationManager());
		
		// URL 권한이 어떻게 되는지 조회
		// 가져온 URL의 권한과 사용자의 ROLE권한으로 AccessVote를 진행한다.
		filterSecurityInterceptor.setSecurityMetadataSource(filterInvocationSecurityMetadataSource());
		
		// Role정보에 따라 URL 접근 허용여부 판단
		filterSecurityInterceptor.setAccessDecisionManager(accessDecisionManager());
		
		
		filterSecurityInterceptor.setApplicationEventPublisher(getApplicationContext());
		
		return filterSecurityInterceptor;
		
	}
	
}
