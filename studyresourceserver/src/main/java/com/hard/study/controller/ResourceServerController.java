package com.hard.study.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hard.study.service.oauth.AuthenticationService;
import com.hard.study.service.oauth.ReactResourceService;
import com.hard.study.vo.oauth.OAuthResourceAuthorityVo;
import com.hard.study.vo.oauth.UserInfoVo;

@Controller
@RequestMapping(value="/authenticated")
public class ResourceServerController {
	
	@Autowired
	private AuthenticationService authenticationService;
	
	@Autowired
	private ReactResourceService reactResourceService;
	
	@ResponseBody
	@RequestMapping(value="/username", method=RequestMethod.POST)
	public UserInfoVo gerUserInfo() throws Exception {
		
		Authentication authentication = authenticationService.getAuthentication();
		
		UserInfoVo userInfoVo = new UserInfoVo();
		userInfoVo.setUsername(authentication.getName());
		
		return userInfoVo;
		
	}
	
	@ResponseBody
	@RequestMapping(value="/authority", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<OAuthResourceAuthorityVo>> authority(@RequestBody OAuthResourceAuthorityVo vo) throws Exception {
		
		List<OAuthResourceAuthorityVo> result = new ArrayList<OAuthResourceAuthorityVo>();
		result = reactResourceService.getUserResource(vo);
		
		return new ResponseEntity<List<OAuthResourceAuthorityVo>>(result, HttpStatus.OK);
		
	}
	
	@RequestMapping(value="/getaccess", method=RequestMethod.GET)
	public String getAccess() throws Exception {
		
		return "hellllllo";
		
	}
	
}
