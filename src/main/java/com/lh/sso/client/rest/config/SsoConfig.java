package com.lh.sso.client.rest.config;

import com.lh.sdk.config.annotation.PropertyConfig;

/**
 * @author wangyongxin
 * @createAt 2018-05-22 上午11:32
 **/
@PropertyConfig(prefix = "sso")
public class SsoConfig {

    private String loginUrl;
    private String prefix;
    private String serviceUrl;
    private String exclude;

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getExclude() {
        return exclude;
    }

    public void setExclude(String exclude) {
        this.exclude = exclude;
    }

}
