package com.leyou.auth.controller;

import com.leyou.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
