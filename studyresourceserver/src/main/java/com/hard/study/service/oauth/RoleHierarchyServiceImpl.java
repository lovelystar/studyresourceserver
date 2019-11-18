package com.hard.study.service.oauth;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hard.study.dao.oauth.RoleHierarchyDao;
import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;
import com.hard.study.vo.oauth.RoleHierarchyVo;

@Service("roleHierarchyService")
public class RoleHierarchyServiceImpl implements RoleHierarchyService {
	
	@Resource(name="roleHierarchyDao")
	private RoleHierarchyDao roleHierarchyDao;
	
	// 계정들의 group정보
	@Override
	public RoleHierarchyVo getRoleHierarchy() {
		
		List<RoleHierarchyVo> roleHierarchyList = roleHierarchyDao.getRoleHierarchy();
		
		RoleHierarchyVo roleHierarchyVo = new RoleHierarchyVo();
		
		String strRoleHierarchy = null;
		Long idNum = 0L;
		
		for(RoleHierarchyVo roleHierarchy : roleHierarchyList) {
			
			if(roleHierarchy.getId() == 1) {
				
				strRoleHierarchy = roleHierarchy.getGroupName() + ">" + roleHierarchy.getGroupAuthority() + ">" + roleHierarchy.getAuthority(); 
				
			} else if(idNum == roleHierarchy.getId()) {
				
				strRoleHierarchy += ">" + roleHierarchy.getAuthority();
				
			} else {
				
				strRoleHierarchy += ">" + roleHierarchy.getGroupName() + ">" + roleHierarchy.getGroupAuthority() + ">" + roleHierarchy.getAuthority();
				
			}
			
			idNum = roleHierarchy.getId();
			
		}
		
		roleHierarchyVo.setRoleHierarchy(strRoleHierarchy);
		
		return roleHierarchyVo;
		
	}
	
	// 인증된 사용자의 URL 권한 정보
	@Override
	public List<OAuthResourceAuthorityVo> getUserResource(OAuthResourceAuthorityVo resourceAuthorityVo) {
		
		return roleHierarchyDao.getUserResource(resourceAuthorityVo);
		
	}
	
}
