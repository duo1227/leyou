package com.leyou.auth.feign;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.task.AppTokenHolder;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrivilegeInterceptor implements RequestInterceptor {

    @Autowired
    private AppTokenHolder appTokenHolder;

    @Autowired
    private JwtProperties prop;

    @Override
    public void apply(RequestTemplate template) {
        // 获取token
        String token = appTokenHolder.getToken();
        // 放入请求头
        template.header(prop.getApp().getHeaderName(), token);
    }
}
