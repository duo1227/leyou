package com.leyou.user.interceptor;

import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.user.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@Component
public class PrivilegeInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties prop;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // 1、获取请求头
            String token = request.getHeader(prop.getApp().getHeaderName());
            // 2、校验
            Payload<AppInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), AppInfo.class);
            // 2.1 取出token中的targetIdList
            List<Long> targetList = payload.getUserInfo().getTargetList();
            // 2.2 获取当前微服务的service_id
            Long currentServiceId = prop.getApp().getId();
            // 2.3 校验当前服务id有没有在targetList中
            if(targetList==null ||  !targetList.contains(currentServiceId)){
                throw new RuntimeException("没有访问权限");
            }
            // 成功：可以访问我们当前服务
            return true;
        } catch (Exception e) {
            log.error("【用户微服务-鉴权拦截器】拒绝访问：出现异常：{}", e.getMessage());
            // 2.4 如果没有在里面返回false，不能继续访问
            return false;
        }
    }
}
