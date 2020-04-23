package com.leyou.gateway.filters;

import com.leyou.gateway.config.JwtProperties;
import com.leyou.gateway.task.AppTokenHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 在请求转发之前，把我们从授权中心申请到的token放入到请求头中
 */
@Component
public class PrivilegeFilter implements GlobalFilter, Ordered {

    @Autowired
    private AppTokenHolder appTokenHolder;

    @Autowired
    private JwtProperties prop;

    /**
     * 主要是把我们申请到的token放入请求头中
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取token
        String token = appTokenHolder.getToken();
        // 把token放入请求头
        ServerHttpRequest request = exchange.getRequest().mutate().header(prop.getApp().getHeaderName(), token).build();
        // 创建新的交换机
        ServerWebExchange build = exchange.mutate().request(request).build();
        // 把新的交换机放入过滤链中
        return chain.filter(build);
    }

    @Override
    public int getOrder() {
        return 6;
    }
}
