package com.lh.sso.client.web;

import org.jasig.cas.client.authentication.AuthenticationFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.util.AbstractConfigurationFilter;
import org.jasig.cas.client.util.AssertionThreadLocalFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;


/**
 * @author wangyongxin
 */
public class FilterChainProxy extends AbstractConfigurationFilter {

	Logger LOG = LoggerFactory.getLogger(getClass());
	
	private Filter[] ssoFilters;
	private String[] ignore_resources;

	/**
	 * 初始化过滤器
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//获取忽略列表
		String exclude = filterConfig.getInitParameter("ignore_resources");
		if(exclude!=null){
			ignore_resources = exclude.split(",");
		}
		WrappedFilterConfig wrappedFilterConfig = new WrappedFilterConfig(filterConfig, getInitParamsFromSsoProperties());
		this.ssoFilters = ObtainAllDefinedFilters().toArray(new Filter[0]);
		for(Filter filter : ssoFilters){
			filter.init(wrappedFilterConfig);
		}
	}
	/**
	 * 读取sso.properties初始化参数（params属性）
	 */
	private Map<String, String> getInitParamsFromSsoProperties(){
		Properties properties = new Properties();
		try {
			ClassLoader loader = WrappedFilterConfig.class.getClassLoader();
			properties.load(loader.getResourceAsStream("sso.properties"));
			Map<String, String> params = new HashMap();
			for (Object obj : properties.keySet()) {
				String key = (String) obj;
				if(key!=null){
					params.put(key.trim(), properties.getProperty(key).trim());
				}
			}
			return params;
		} catch (IOException e) {
			LOG.error("init WrappedFilterConfig failure",e);
		}
		return null;
	}
	
	/**
	 * 初始化过滤器链
	 */
	private List<Filter> ObtainAllDefinedFilters() {
		List<Filter> ssoList = new ArrayList<Filter>();
		//cas的单点登出
		ssoList.add(new SingleSignOutFilter());
		ssoList.add(new AuthenticationFilter());
		ssoList.add(new Cas20ProxyReceivingTicketValidationFilter());
		ssoList.add(new AssertionThreadLocalFilter());
		ssoList.add(new HttpServletRequestWrapperFilter());
		return ssoList;
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		String currentResource =  req.getRequestURI();

		if (this.ssoFilters != null && this.ssoFilters.length == 0) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(req.getRequestURL() + " has an empty filter list");
			}
			chain.doFilter(request, response);
			return;
		}

		FilterInvocation fi = new FilterInvocation(request, response, chain);
		if(currentResource!=null&&!isIgnored(currentResource.toLowerCase())){
			VirtualFilterChain virtualFilterChain = new VirtualFilterChain(fi,this.ssoFilters);
			virtualFilterChain.doFilter(fi.getRequest(), fi.getResponse());
		}else{
			chain.doFilter(req, response);
		}
	}  

	/**
	 * 销毁
	 */
	@Override
	public void destroy() {
		if(this.ssoFilters != null && this.ssoFilters.length > 0){
			for (int i=0,len = this.ssoFilters.length;i<len; i++) {
				Filter filter = this.ssoFilters[i];
				if(filter!=null){
					if(LOG.isDebugEnabled()){
						LOG.debug("Destroying Filter defined in ApplicationContext: '" + filter.toString() + "'");
					}
					filter.destroy();
				}
			}
		}
	}
	
	/**
	 * 过滤器代理链
	 */
	private class VirtualFilterChain implements FilterChain {
		private FilterInvocation fi;
		private Filter[] additionalFilters;
		private int currentPosition = 0;

		public VirtualFilterChain(FilterInvocation filterInvocation,
				Filter[] additionalFilters) {
			this.fi = filterInvocation;
			this.additionalFilters = additionalFilters;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response)
				throws IOException, ServletException {
			if (currentPosition == additionalFilters.length) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(fi.getRequestUrl()
							+ " reached end of additional filter chain; proceeding with original chain");
				}
				fi.getChain().doFilter(request, response);
			} else {
				currentPosition++;
				if (LOG.isDebugEnabled()) {
					LOG.debug(fi.getRequestUrl() + " at position "
							+ currentPosition + " of "
							+ additionalFilters.length
							+ " in additional filter chain; firing Filter: '"
							+ additionalFilters[currentPosition - 1] + "'");
				}
				additionalFilters[currentPosition - 1].doFilter(request,
						response, this);
			}
		}
	}

	/**
	 * 判断是否忽略
	 * @param requestUrl
	 * @return
	 * @author
	 */
	private  boolean isIgnored(String requestUrl) {
		if (ignore_resources == null){
			return false;
		}else{
			for (String suffix : ignore_resources) {
				if (suffix != null && !"".equals(suffix) && requestUrl.endsWith(suffix.trim().toLowerCase())) {
					return true;
				}
			} 
			return false;
		}
	}

}

