package com.hard.study.dao.oauth;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;
import com.hard.study.vo.oauth.RoleHierarchyVo;

@Repository("roleHierarchyDao")
public class RoleHierarchyDaoImpl implements RoleHierarchyDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void setSqlSession(SqlSession sqlSession) {
		
		this.sqlSession = sqlSession;
		
	}
	
	// 계정들의 group정보
	@Override
	public List<RoleHierarchyVo> getRoleHierarchy() {
		
		return sqlSession.selectList("getRoleHierarchy");
		
	}
	
	// 인증된 사용자의 URL 권한 정보
	@Override
	public List<OAuthResourceAuthorityVo> getUserResource(OAuthResourceAuthorityVo resourceAuthorityVo) {
		
		return sqlSession.selectList("getUserResource", resourceAuthorityVo);
		
	}
}
