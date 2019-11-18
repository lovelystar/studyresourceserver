package com.hard.study.dao.oauth;

import java.util.List;

import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;
import com.hard.study.vo.oauth.RoleHierarchyVo;

public interface RoleHierarchyDao {
	
	// 계정들의 group정보
	public List<RoleHierarchyVo> getRoleHierarchy();
	
	// 인증된 사용자의 URL 권한 정보
	public List<OAuthResourceAuthorityVo> getUserResource(OAuthResourceAuthorityVo resourceAuthorityVo);
	
}
