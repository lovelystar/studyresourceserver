package com.hard.study.utils.encryption;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AES256Cipher {
	
	private static final String secretKey = "$2a$10$swnbjAczM4QpQKoB0MHFj.liH"; //32 고정
	private static final String IV = "vector"; 									//16 유동
	private static Key secretKeySpec;
	private static IvParameterSpec ivSpec;
	
	public static void setSecretKeySpec (Key secretKeySpec) {
		
		AES256Cipher.secretKeySpec = secretKeySpec;
		
	}
	
	public static void setIvSpec (IvParameterSpec ivSpec) {
		
		AES256Cipher.ivSpec = ivSpec;
		
	}
	
	public static void createSecretKey (String secretKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException {
		
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		
		byte[] keyBytes = secretKey.getBytes("UTF-8");
		byte[] saltBytes = digest.digest(keyBytes);
		
		// 256bit (AES256은 256bit의 키, 128bit의 블록사이즈를 가짐.)
		PBEKeySpec pbeKeySpec = new PBEKeySpec(secretKey.toCharArray(), saltBytes, 65536, 256);
		Key createSecretKey = factory.generateSecret(pbeKeySpec);
		
		// 256bit = 32byte
		byte[] key = new byte[32];
		System.arraycopy(createSecretKey.getEncoded(), 0, key, 0, 32);
		
		// AES 알고리즘을 적용하여 암호화키 생성
		SecretKeySpec secret = new SecretKeySpec(key, "AES");
		setSecretKeySpec(secret);
		
	}
	
	public static void createIvSpec (String IV) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeySpecException {
		
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		MessageDigest digest = MessageDigest.getInstance("SHA-512");
		
		byte[] vectorBytes = IV.getBytes("UTF-8");
		byte[] saltBytes = digest.digest(vectorBytes);
		
		// 128bit
		PBEKeySpec pbeKeySpec = new PBEKeySpec(IV.toCharArray(), saltBytes, 65536, 128);
		Key secretIV = factory.generateSecret(pbeKeySpec);
		
		// 128bit = 16byte
		byte[] iv = new byte[16];
		System.arraycopy(secretIV.getEncoded(), 0, iv, 0, 16);
		
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		setIvSpec(ivSpec);
	
	}
	
	public static String AES_Encrypt(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		
		if ( secretKeySpec == null ) {
			
			createSecretKey(secretKey);
			
		}
		
		createIvSpec(IV);
		
		c.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
		byte[] encrypted = c.doFinal(str.getBytes("UTF-8"));
		String enStr = new String(Base64.encodeBase64(encrypted));
		
		return enStr;
		
	}
	
	public static String AES_Decrypt(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
		
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		
		if ( secretKeySpec == null ) {
			
			createSecretKey(secretKey);
			
		}
		
		createIvSpec(IV);
		
		c.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
		byte[] byteStr = Base64.decodeBase64(str.getBytes());
		
		return new String(c.doFinal(byteStr),"UTF-8");
		
	}
	
}
