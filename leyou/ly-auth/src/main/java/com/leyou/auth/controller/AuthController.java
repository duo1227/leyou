package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import com.leyou.common.auth.entity.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;


    /**
     * 用户登录接口
     * @param username  用户名称
     * @param password  密码
     * @return          无
     */
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username") String username,
                                      @RequestParam("password") String password,
                                      HttpServletResponse response
    ){
        authService.login(username, password, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 验证登陆用户
     * @param request 通过请求对象获取cookie中的token
     * @return 返回token中的用户信息
     */
    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request, HttpServletResponse response){

        UserInfo userInfo = authService.verify(request, response);
        return ResponseEntity.ok(userInfo);
    }


    /**
     * 退出登录
     * @param request   用来获取cookie中的token
     * @param response  用来删除浏览器的cookie
     * @return          无
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request,HttpServletResponse response){
        authService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 给其他微服务申请token用的
     * @param id        微服务自己的id
     * @param secret    微服务自己的密码
     * @return          返回jwt的token
     */
    @GetMapping("/authorization")
    public ResponseEntity<String> authorization(@RequestParam("id") Long id,@RequestParam("secret") String secret){
        String token = authService.authorization(id, secret);
        return ResponseEntity.ok(token);
    }

}
