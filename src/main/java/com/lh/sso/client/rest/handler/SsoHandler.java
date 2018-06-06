package com.lh.sso.client.rest.handler;

import com.jfinal.handler.Handler;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.lh.common.sysmanager.SimpleEmpVo;
import com.lh.sdk.config.ConfigManager;
import com.lh.sdk.utils.HandlerKit;
import com.lh.sdk.exception.SystemException;
import com.lh.sdk.web.model.ResponseData;
import com.lh.sso.client.constants.SsoConstant;
import com.lh.sso.client.rest.config.SsoConfig;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.pac4j.cas.client.rest.CasRestFormClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.profile.CasProfile;
import org.pac4j.cas.profile.CasRestProfile;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.credentials.TokenCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author wangyongxin
 * @createAt 2018-05-21 下午3:18
 **/
public final class SsoHandler extends Handler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SsoHandler.class);

    private Cache tokenCache ;

    private SsoConfig ssoConfig;

    public SsoHandler(){
        init();
    }

    private void init(){
        this.tokenCache = CacheKit.getCacheManager().getCache("token");
        this.ssoConfig = ConfigManager.me().get(SsoConfig.class);
    }

    @Override
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
        //跳过options请求
        isHandled[0] = true;
        if("OPTIONS".equals(request.getMethod())){
            this.next.handle(target,request,response,isHandled);
            return;
        }
        String uri = request.getRequestURI();
        if(isExclude(uri)){
            this.next.handle(target,request,response,isHandled);
            return;
        }
        isHandled[0] = true;
        final String accessToken = request.getHeader(SsoConstant.AUTH_TOKEN_IDENTITY);
        if(StrKit.isBlank(accessToken)){
            //accessToken均不存在，返回未认证
            ResponseData<String> res = new ResponseData<>(ResponseData.AJAX_STATUS_NOT_AUTH,"未认证授权");
            HandlerKit.renderJsonResult(res,request,response,isHandled);
        } else {
            //accessToken存在，检查用户缓存
            Element element = tokenCache.get(accessToken);
            if(element != null){
                Object objectValue = element.getObjectValue();
                if(objectValue!=null){
                    if(next!=null){
                        next.handle(target,request,response,isHandled);
                    }
                    return;
                }
                tokenCache.remove(accessToken);
            }
            //用户缓存不存在
            String userId = request.getParameter("userId");
            if(StrKit.isBlank(userId)){
                userId = "";
            }
            //进行认证
            try {
                CasRestFormClient casRestClient = new CasRestFormClient();
                casRestClient.setConfiguration(new CasConfiguration(ssoConfig.getLoginUrl(),ssoConfig.getPrefix()));
                J2EContext context = new J2EContext(request, response);
                casRestClient.init(context);
                CasRestProfile casRestProfile = new CasRestProfile(accessToken, userId);
                TokenCredentials tokenCredentials = casRestClient.requestServiceTicket(ssoConfig.getServiceUrl(), casRestProfile, context);
                CasProfile casProfile = casRestClient.validateServiceTicket(ssoConfig.getServiceUrl(), tokenCredentials, context);
                Element userInfo = new Element(accessToken, assembleEmpInfo(casProfile),7200,3600 * 8);
                userInfo.setVersion(System.currentTimeMillis());
                tokenCache.put(userInfo);
                if(next!=null){
                    next.handle(target,request,response,isHandled);
                }
            } catch (Exception e) {
                LOGGER.error("sso认证失败",e);
                HandlerKit.renderJsonResult(new ResponseData<>(ResponseData.AJAX_STATUS_AUTH_EXPIRED,"认证超时，请重新认证"),request,response,isHandled);
            }
        }
    }

    private SimpleEmpVo assembleEmpInfo(CasProfile casProfile) {
        Map<String, Object> attributes = casProfile.getAttributes();
        SimpleEmpVo simpleEmpVo = new SimpleEmpVo();
        simpleEmpVo.setUserCode((String) attributes.get("userCode"));
        simpleEmpVo.setUserId((String) attributes.get("userId"));
        simpleEmpVo.setUserName((String) attributes.get("userName"));
        return simpleEmpVo;
    }

    private boolean isExclude(String uri) {
        if(StrKit.isBlank(uri)){
            return true;
        }
        String exclude = this.ssoConfig.getExclude();
        if(!StrKit.isBlank(exclude)){
            String[] excludes = exclude.split(",");
            for(String suffix : excludes){
                if(!StrKit.isBlank(suffix.trim()) && uri.endsWith(suffix.trim().toLowerCase())){
                    return true;
                }
            }
        }
        return false;
    }
}
