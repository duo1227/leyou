package com.leyou.gateway.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("auth-service")
public interface AuthClient {

    /**
     * 给其他微服务申请token用的
     * @param id        微服务自己的id
     * @param secret    微服务自己的密码
     * @return          返回jwt的token
     */
    @GetMapping("/authorization")
    public String authorization(@RequestParam("id") Long id, @RequestParam("secret") String secret);
}
