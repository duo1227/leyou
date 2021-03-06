package com.leyou.user.controller;

import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Slf4j
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     *   数据校验
     * @param data 校验手机或者用户名
     * @param type 1.用户名  2.手机
     * @return
     */
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> check(@PathVariable("data") String data,@PathVariable("type") Integer type){

        Boolean bool = userService.check(data, type);
        return ResponseEntity.ok(bool);
    }


    /**
     * 根据用户手机号码发送短信验证码
     * @param phone
     * @return
     */
    @PostMapping("/code")
    public ResponseEntity<Void> sendCode(@RequestParam("phone") String phone){
        userService.sendCode(phone);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 用户注册
     * @param user 用户数据
     * @param code 验证码
     * @return
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid User user,
                                         @RequestParam("code") String code){

        userService.register(user,code);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


    @Autowired
    private HttpServletRequest request;

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    public ResponseEntity<UserDTO> queryUserByUserNameAndPassword(
            @RequestParam("username") String username,
            @RequestParam("password") String password
            ){

        log.info("【用户微服务】获取请求头信息，privilege_token = {}", request.getHeader("privilege_token"));
        UserDTO userDTO = userService.queryUserByUserNameAndPassword(username,password);
        return ResponseEntity.ok(userDTO);
    }
}
