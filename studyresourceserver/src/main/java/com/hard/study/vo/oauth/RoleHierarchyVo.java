package com.hard.study.vo.oauth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class RoleHierarchyVo {
	
	private String username;
	private String authority;
	private Long groupId;
	private String groupAuthority;
	private Long id;
	private String groupName;
	private String roleHierarchy;
	
}
