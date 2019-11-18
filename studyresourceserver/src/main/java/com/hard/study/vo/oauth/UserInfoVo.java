package com.hard.study.vo.oauth;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UserInfoVo implements UserDetails {
	
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String password;
//	private List<UserAuthorityVo> userAuthorityList;
//	private List<GroupsAuthorityVo> groupAuthorityList;
	private List<OAuthResourceAuthorityVo> resourceAuthorityList;
//	private Set<GrantedAuthority> authorities;
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public List<OAuthResourceAuthorityVo> getResourceAuthorityList() {
		return resourceAuthorityList;
	}

	public void setResourceAuthorityList(List<OAuthResourceAuthorityVo> resourceAuthorityList) {
		this.resourceAuthorityList = resourceAuthorityList;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		// TODO Auto-generated method stub
		return null;
		
	}
	
	@Override
	public String getPassword() {
		
		// TODO Auto-generated method stub
//		return null;
		return password;
		
	}
	
	@Override
	public String getUsername() {
		
		// TODO Auto-generated method stub
//		return null;
		return username;
		
	}
	
	@Override
	public boolean isAccountNonExpired() {
		
		// TODO Auto-generated method stub
		return false;
		
	}
	
	@Override
	public boolean isAccountNonLocked() {
		
		// TODO Auto-generated method stub
		return false;
		
	}
	
	@Override
	public boolean isCredentialsNonExpired() {
		
		// TODO Auto-generated method stub
		return false;
		
	}
	
	@Override
	public boolean isEnabled() {
		
		// TODO Auto-generated method stub
		return false;
		
	}
	
}
