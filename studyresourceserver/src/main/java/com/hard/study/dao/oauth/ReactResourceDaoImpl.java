package com.hard.study.dao.oauth;

import java.util.List;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;

@Repository("reactResourceDao")
public class ReactResourceDaoImpl implements ReactResourceDao {
	
	@Autowired
	private SqlSession sqlSession;
	
	public void setSqlSession(SqlSession sqlSession) {
		
		this.sqlSession = sqlSession;
				
	}
	
	@Override
	public List<OAuthResourceAuthorityVo> getUserResource(OAuthResourceAuthorityVo vo) throws Exception {
		
		return sqlSession.selectList("getUserResource", vo);
		
	}
	
}