package com.hard.study.vo.oauth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class OAuthResourceAuthorityVo {
	
	private Integer idxOauthResource;
	private Integer idxOauthResourceAuthority;
	private String resourceId;			// 리소스 ID
	private String resourceName;		// 리소스 이름
	private String resourcePattern;		// 리소스 패턴
	private String resourceType;		// 리소스 타입(url, method)
	private String httpMethod;			// httpMethod
	private Integer sortOrder;			// 리소스 순서
	private String userName;			// 사용자 이름
	private String authority;			// 권한 코드
	
}
