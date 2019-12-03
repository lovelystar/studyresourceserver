package com.hard.study.service.oauth;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.hard.study.dao.oauth.ReactResourceDao;
import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;

@Service("reactResourceService")
public class ReactResourceServiceImpl implements ReactResourceService {
	
	@Resource(name="reactResourceDao")
	private ReactResourceDao reactResourceDao;
	
	@Override
	public List<OAuthResourceAuthorityVo> getUserResource(OAuthResourceAuthorityVo vo) throws Exception {
		
		return reactResourceDao.getUserResource(vo);
		
	}
	
}
