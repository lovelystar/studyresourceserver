package com.hard.study.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {
	
	@RequestMapping(value="/test2")
	public String testTwo() throws Exception {
		
		System.out.println("test2");
		return "test2";
	}
	
}
