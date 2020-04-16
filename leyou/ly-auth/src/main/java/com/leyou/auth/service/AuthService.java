package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;

    /**
     * 用户登录
     * @param username
     * @param password
     * @param response
     */
    public void login(String username, String password, HttpServletResponse response) {

        try {
            //1.远程调用用户中心，校验账号密码
            UserDTO userDTO = userClient.queryUserByUserNameAndPassword(username, password);
            //2.生成JWT
            UserInfo userInfo = new UserInfo(userDTO.getId(),userDTO.getUsername(),"guest");
            String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), 30);
            //3.3、写入cookie中：写入浏览器的cookie中
            CookieUtils.newCookieBuilder().name(
                    prop.getUser().getCookieName())
                    .value(token)
                    .domain(prop.getUser().getCookieDomain())
                    .httpOnly(true)
                    .response(response).build();

        } catch (Exception e) {
            log.error("【授权微服务】登录失败，用户名或者密码错误", e);
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }


    }
}
