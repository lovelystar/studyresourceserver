package com.hard.study.service.oauth;

import java.util.List;

import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;

public interface ReactResourceService {
	
	public List<OAuthResourceAuthorityVo> getUserResource(OAuthResourceAuthorityVo vo) throws Exception;
	
}
