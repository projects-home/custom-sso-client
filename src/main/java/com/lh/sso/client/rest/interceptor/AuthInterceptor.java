package com.lh.sso.client.rest.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.lh.sdk.web.model.ResponseData;
import com.lh.sso.client.constants.SsoConstant;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 * @author wangyongxin
 * @createAt 2018-05-22 上午11:14
 **/
public class AuthInterceptor implements Interceptor {

    private Cache tokenCache;

    public AuthInterceptor(){
        this.tokenCache = CacheKit.getCacheManager().getCache("token");
    }
    @Override
    public void intercept(Invocation inv) {
        Controller controller = inv.getController();
        String accessToken = controller.getRequest().getHeader(SsoConstant.AUTH_TOKEN_IDENTITY);
        if(!StrKit.isBlank(accessToken)){
            Element element = tokenCache.get(accessToken);
            if(element!=null&&!element.isExpired()){
                inv.invoke();
                return;
            }
            controller.renderJson(new ResponseData<>(ResponseData.AJAX_STATUS_AUTH_EXPIRED,"认证过期，请重新认证"));
            return;
        }
        controller.renderJson(new ResponseData<>(ResponseData.AJAX_STATUS_NOT_AUTH,"未认证授权，请先进行认证"));
    }
}
