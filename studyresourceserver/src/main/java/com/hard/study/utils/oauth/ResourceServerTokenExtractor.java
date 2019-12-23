package com.hard.study.utils.oauth;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.hard.study.utils.encryption.AES256Cipher;

public class ResourceServerTokenExtractor implements TokenExtractor {
	
	private static AES256Cipher aes = new AES256Cipher();
	
	@Override
	public Authentication extract(HttpServletRequest request) {
		
		String tokenValue = extractToken(request);
		
		// Custom한 토큰을 제대로 추출했으면
		if(tokenValue != null) {
			
			PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(tokenValue, "");
			return authentication;
			
		}
		
		return null;
		
	}
	
	protected String extractToken(HttpServletRequest request) {
		
		String token = extractHeader(request);
		
		// Custom한 토큰을 제대로 추출하지 못했다면
		if(token == null) {
			
			System.out.println("Token not fount in headers. Check Your Parameters.");
			request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, OAuth2AccessToken.BEARER_TYPE);
			
		}
		
		return token;
	}
	
	protected String extractHeader(HttpServletRequest request) {
		
		// custom Token의 길이
		int tokenLength = request.getHeader("Authorization").length();
		
		// Bearer suminToken을 뗀 그 뒷부분 ( 실제 accessToken부분 )
		String accessToken = request.getHeader("Authorization").substring(17, tokenLength);
		
		String tokenValue = null;
		String tokenType = null;
		String tokenStr = null;
		
		try {
			
			// 복호화
			tokenValue = aes.AES_Decrypt(accessToken);
			System.out.println("tokenValue = " + tokenValue);
			// Bearer
			tokenType = request.getHeader("Authorization").substring(0, 6);
			
		} catch (InvalidKeyException | UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
				| InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException
				| InvalidKeySpecException e) {
			
			e.printStackTrace();
			
		}
		
		Enumeration<String> headers = request.getHeaders("Authorization");
		while(headers.hasMoreElements()){
			
			String value = headers.nextElement();
			if(value.indexOf("Bearer") >= 0) {
				
				// Bearer + 복호화된 acccessToken
				tokenStr = tokenType + " " + tokenValue;
				value = tokenStr;
				
			}
			
			if((value.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase()))) {
				
				String authHeaderValue = value.substring(OAuth2AccessToken.BEARER_TYPE.length()).trim();
				
				// access token Type을 bearer로 세팅
				request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_TYPE, value.substring(0, OAuth2AccessToken.BEARER_TYPE.length()));
				int commaIndex = authHeaderValue.indexOf(",");
				if(commaIndex > 0) {
					authHeaderValue = authHeaderValue.substring(0, commaIndex);
				}
				
				return authHeaderValue;
				
			}
			
		}
		
		return null;
		
	}
	
}
