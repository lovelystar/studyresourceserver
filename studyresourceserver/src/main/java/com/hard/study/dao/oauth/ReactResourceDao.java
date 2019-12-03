package com.hard.study.dao.oauth;

import java.util.List;

import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;

public interface ReactResourceDao {
	
	public List<OAuthResourceAuthorityVo> getUserResource(OAuthResourceAuthorityVo vo) throws Exception;
	
}
