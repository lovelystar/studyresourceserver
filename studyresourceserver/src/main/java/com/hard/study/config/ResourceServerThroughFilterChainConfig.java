package com.hard.study.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

@Configuration
public class ResourceServerThroughFilterChainConfig implements FilterChain {
	
	@Nullable
	private Filter filter;
	
	@Nullable
	private FilterChain filterChain;
	
	@Nullable
	private Servlet servlet;
	
	public ResourceServerThroughFilterChainConfig() {
		
	}
	
	public void setResourceServerThroughFilterChain(Filter filter, FilterChain filterChain) {
		
		Assert.notNull(filter, "Filter must not be null");
		Assert.notNull(filterChain, "FilterChain must not be null");
		
		this.filter=filter;
		this.filterChain=filterChain;
		
	}
	
	public void setResourceServerThroughServlet(Servlet servlet) {
		
		Assert.notNull(servlet, "Servlet must not be null");
		this.servlet=servlet;
		
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
		
		if(this.filter != null) {
			
			this.filter.doFilter(request, response, this.filterChain);
			
		} else {
			
			Assert.state(this.servlet != null, "Neither a Filter not a Servlet set");
			this.servlet.service(request, response);
			
		}
		
	}
	
}
