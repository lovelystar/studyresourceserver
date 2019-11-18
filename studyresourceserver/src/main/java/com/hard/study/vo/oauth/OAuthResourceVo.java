package com.hard.study.vo.oauth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class OAuthResourceVo {
	
	private Long idx;
	private String resourceId;
	private String resourceName;
	private String resourcePattern;
	private String resourceType;
	private Integer sortOrder;
	private String httpMethod;
	
}
