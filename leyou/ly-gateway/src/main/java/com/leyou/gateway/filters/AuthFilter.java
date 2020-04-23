package com.leyou.gateway.filters;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtProperties  prop;

    @Autowired
    private FilterProperties filterProp;

    /**
     * 权限校验： 网关过滤器中去校验用户的token
     * @param exchange      交换机
     * @param chain         过滤器链
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("【乐优网关】过滤器执行了....");
        // 0、先判断当前url有没有在白名单中，如果白名单存在这个url，不用去校验了，直接放行
        if(isAllowPath(exchange.getRequest())){
            log.info("【乐优网关】白名单放行：{}", exchange.getRequest().getURI().getPath());
            return chain.filter(exchange);
        }

        // 1、获取cookie
        HttpCookie cookie = exchange.getRequest().getCookies().getFirst(prop.getUser().getCookieName());
        try {
            // 2、校验token
            if (cookie != null) {
                // 3、取出token的用户的角色
                log.info("获取token：key={},value={}", cookie.getName(), cookie.getValue());
                Payload<UserInfo> payload = JwtUtils.getInfoFromToken(cookie.getValue(), prop.getPublicKey(), UserInfo.class);
                // 3.1 获取当前用户
                UserInfo userInfo = payload.getUserInfo();
                log.info("【乐优网关】用户对象： {}， 他的角色：{}",userInfo, userInfo.getRole());
                // 4、校验用户是否有访问当前URL的权限
                ServerHttpRequest request = exchange.getRequest();
                // TODO： 待完善的，判断当前用户是否有访问当前URL的权限
                // 4.1 获取当前访问的URL
                log.info("【乐优网关】当前用户：{}，访问了：{}， 方法类型：{}",userInfo, request.getURI().getPath(),request.getMethodValue());
            } else {
                // 如果cookie为空不给访问
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }
        }catch (Exception e){
            // 5、如果没有权限，阻止继续访问
            log.error("【乐优网关】校验token失败：{}",e.getMessage(),e);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);// 状态码
            return exchange.getResponse().setComplete();// 阻止往下执行
        }
        // 6、如果有权限，放行
        return chain.filter(exchange);
    }

    /**
     * 判断当前url是否是白名单
     * @param request   请求对象，用来获取当前请求的URL
     * @return
     */
    private boolean isAllowPath(ServerHttpRequest request) {
        // 获取url地址
        String path = request.getURI().getPath();
        // 获取白名单
        List<String> allowPaths = filterProp.getAllowPaths();
        for (String allowPath : allowPaths) {
            if(path.startsWith(allowPath)){
                // 如果以白名单开头，放行
                return true;
            }
        }
        return false;
    }

    /**
     * 数字越小越先执行
     *      1、第一个执行： Integer.MIN_VALUE
     *      2、最后一个执行： Integer.MAX_VALUE
     */
    @Override
    public int getOrder() {
        return 0;
    }
}