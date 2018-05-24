package com.lh.sso.client.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * 包装FilterConfig，使用配置文件读取到的参数初始化Filter的initParam
 * @author
 */
public class WrappedFilterConfig implements FilterConfig {

	private static final Logger LOG = LoggerFactory.getLogger(WrappedFilterConfig.class);
	private Map<String, String> params;
	private FilterConfig filterConfig;
	
	/**
	 * @param currentFilterConfig
	 * @param params
	 */
	public WrappedFilterConfig(FilterConfig currentFilterConfig, Map<String, String> params) {
		this.filterConfig = currentFilterConfig;
		this.params = params;
	}

	@Override
	public String getFilterName() {
		return filterConfig.getFilterName();
	}

	/**
	 * 获取初始化参数
	 */
	@Override
	public String getInitParameter(String key) {
		String value = filterConfig.getInitParameter(key);
		if(value!=null){
			return value;
		}
		return params!=null?params.get(key):null;
	}

	/**
	 * 获取初始化枚举
	 */
	@Override
	public Enumeration<String> getInitParameterNames() {
		final Iterator<String> iterator = params.keySet().iterator();
		return new Enumeration<String>() {

			@Override
			public boolean hasMoreElements() {
				return iterator.hasNext();
			}

			@Override
			public String nextElement() {
				return iterator.next();
			}
		};
	}

	/**
	 * 获取上下文
	 */
	@Override
	public ServletContext getServletContext() {
		return filterConfig.getServletContext();
	}

}
