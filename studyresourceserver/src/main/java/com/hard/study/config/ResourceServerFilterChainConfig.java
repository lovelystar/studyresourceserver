package com.hard.study.config;

import javax.servlet.Filter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.PriorityOrdered;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationManager;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;

// 빈 후 처리기
// 빈의 생명주기와 빈 팩토리의 생명주기에 관여
// 빈이 초기화되기 전, 초기화된 후 2개의 기회를 제공
// 빈 프로퍼티의 유효성 검사
// Processing :
// 1. bean 생성 + factory 메소드 생성
// 2. Bean Property에 값과 레퍼런스 설정
// 3. Aware Interface에 정의된 Setter 호출
// 4. 빈 인스턴스를 postProcessBeforeInitialization()에 전달
// 5. 초기화 callback 호출
// 6. 빈 인스턴스를 postProcessAfterInitialization()에 전달
// 7. 사용준비 완료
// 8. 컨테이너 종료 후 소멸 Callback 호출
@Configuration
public class ResourceServerFilterChainConfig implements BeanPostProcessor, PriorityOrdered {
	
	// 어떠한 작업
	
	// primary 어노테이션으로 Bean을 미리 만들어뒀기 때문에 사용할 수 있음.
	private static DefaultTokenServices tokenServices;
	
	public void setTokenServices(DefaultTokenServices tokenServices) {
		this.tokenServices = tokenServices;
	}
	
	// 빈이 초기화되기 전 처리
	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		
		return bean;
		
	}
	
	// 빈이 초기화된 후 처리
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		
		// instanceof = 인스턴스의 타입을 확인하기 위해서 사용
		// (ex) 참조변수 instanceof 피연산자 ( 타입 )
		// springSecurityFilterChain를 찾는다.
		if(bean instanceof FilterChainProxy) {
			
			FilterChainProxy filterChainProxy = (FilterChainProxy) bean;
			
			for(SecurityFilterChain securityFilterChain : filterChainProxy.getFilterChains()) {
				
				for(Filter filter : securityFilterChain.getFilters()) {
					// OAuth2AuthenticationProcessingFilter
					// resource서버에 들어왔을 때 사전 인증을 진행한다.
					// 들어온 token을 검증하고, spring security의 인증 context를 채워줌.
					
					// 최종 정리 : Bean을 만들 때 OAuth2AuthenticationProcessingFilter
					// 찾아서 어떻게 token을 추출하고 검증할 것인지 세팅하는 작업을 한다.
					if(filter instanceof OAuth2AuthenticationProcessingFilter) {
						
						OAuth2AuthenticationProcessingFilter oauth2Filter = (OAuth2AuthenticationProcessingFilter) filter;
						
						final OAuth2AuthenticationManager oauth2Manager = new OAuth2AuthenticationManager();
						oauth2Manager.setTokenServices(tokenServices);
						
						oauth2Filter.setAuthenticationManager(oauth2Manager);
						oauth2Filter.setTokenExtractor(new BearerTokenExtractor());
						oauth2Filter.setStateless(true);
						
					}
					
				}
				
			}
			
		
		}
		
		return bean;
		
	}
	
	@Override
	public int getOrder() {
		
		// TODO Auto-generated method stub
		return PriorityOrdered.LOWEST_PRECEDENCE;
		
	}

}
