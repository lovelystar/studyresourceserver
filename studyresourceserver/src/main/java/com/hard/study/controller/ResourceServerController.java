package com.hard.study.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hard.study.service.oauth.AuthenticationService;
import com.hard.study.vo.oauth.UserInfoVo;

@Controller
@RequestMapping(value="/authenticated")
public class ResourceServerController {
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@ResponseBody
	@RequestMapping(value="/username", method=RequestMethod.POST)
	public UserInfoVo gerUserInfo() throws Exception {
		
		Authentication authentication = authenticationService.getAuthentication();
		
		UserInfoVo userInfoVo = new UserInfoVo();
		userInfoVo.setUsername(authentication.getName());
		
		return userInfoVo;
		
	}
	
	@RequestMapping(value="/getaccess", method=RequestMethod.GET)
	public String getAccess() throws Exception {
		
		return "hellllllo";
		
	}
	
}
