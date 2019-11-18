package com.hard.study.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

import com.hard.study.service.oauth.RoleHierarchyService;
import com.hard.study.vo.oauth.RoleHierarchyVo;

// 사용자의 Role정보 가져오는 부분
public class ResourceServerRoleHierarchyConfig {
	
	@Autowired
	private RoleHierarchyService roleHierarchyService;
	
	private RoleHierarchyImpl roleHierarchyImpl;
	
	public ResourceServerRoleHierarchyConfig() {
		
		super();
		roleHierarchyImpl = new RoleHierarchyImpl();
		
	}
	
	public RoleHierarchyImpl getRoleHierarchyImpl() {
		
		getRoleHierarchy();
		return roleHierarchyImpl;
		
	}
	
	public void getRoleHierarchy() {
		
		RoleHierarchyVo roleHierarchyVo = roleHierarchyService.getRoleHierarchy();
		roleHierarchyImpl.setHierarchy(roleHierarchyVo.getRoleHierarchy());
		
	}
	
	public void setRoleHierarchy(String roleHierarchy) {
		
		roleHierarchyImpl.setHierarchy(roleHierarchy);
		
	}
	
}
