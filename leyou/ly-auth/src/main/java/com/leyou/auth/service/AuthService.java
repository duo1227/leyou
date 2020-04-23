package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.entity.ApplicationInfo;
import com.leyou.auth.mapper.ApplicationInfoMapper;
import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ApplicationInfoMapper infoMapper;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
            String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), prop.getUser().getExpire());
            //3.3、写入cookie中：写入浏览器的cookie中
            CookieUtils.newCookieBuilder()
                    .name(prop.getUser().getCookieName())
                    .value(token)
                    .domain(prop.getUser().getCookieDomain())
                    .httpOnly(true)
                    .response(response).build();

        } catch (Exception e) {
            log.error("【授权微服务】登录失败，用户名或者密码错误", e);
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }


    }

    /**
     * 验证登陆用户
     * @param request 通过请求对象获取cookie中的token
     * @return 返回token中的用户信息
     */
    public UserInfo verify(HttpServletRequest request,HttpServletResponse response) {

        try {
            // 1、获取cookie中的token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            // 2、校验token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);

            // 2.0  去redis中取出黑名单token看下是否存在，如果有黑名单，说明是非法登录，抛异常
            // redisTemplate.opsForValue().get(payload.getId());
            Boolean bool = redisTemplate.hasKey(payload.getId());
            if(bool!=null && bool){
                log.warn("【授权中心】用户登录校验，token在redis的黑名单中，阻止它登录！");
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }

            // 2.1 如果token能解析出来，我们要判断这个token的过期时间是否还剩下10分钟内，如果在10分钟内生成一个新的token
            // 过期时间减去10分钟  : 举例： 10:30  - 10  =  10:20
            DateTime dateTime = new DateTime(payload.getExpiration()).minusMinutes(prop.getUser().getRefreshTime());
            // 还剩下十分钟
            if(dateTime.isBeforeNow()){
                // 重新生成token返回给页面
                String newtoken = JwtUtils.generateTokenExpireInMinutes(payload.getUserInfo(), prop.getPrivateKey(), prop.getUser().getExpire());
                // 3、写入cookie中：写入浏览器的cookie中
                CookieUtils.newCookieBuilder()
                        .name(prop.getUser().getCookieName())
                        .value(newtoken)
                        .domain(prop.getUser().getCookieDomain())
                        .httpOnly(true)
                        .response(response).build();
            }

            //3.返回UserInfo
            return payload.getUserInfo();
        } catch (Exception e) {
            log.error("【授权中心】校验token失败：{}", e.getMessage(),e);
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }


    /**
     * 退出登录
     * @param request   用来获取cookie中的token
     * @param response  用来删除浏览器的cookie
     * @return          无
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        //拿到token
        String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());

        //校验token
        Payload<Object> payload = null;
        try {
            payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
        }catch (Exception e){
            // 如果有异常，说明token已经失效，没有必要往下走
            log.error("【授权中心】用户退出，校验token，这个token已经失效了！");
            return;
        }

        //获取token剩余的时间: 减去 当前时间
        long remainTime = payload.getExpiration().getTime() - System.currentTimeMillis();
        // 把这个token存入redis黑名单中
        if(remainTime > 3000){
            // 如果剩余时间大于3秒，存入redis : 值就是1，作为标记
            redisTemplate.opsForValue().set(payload.getId(), "1", remainTime, TimeUnit.MILLISECONDS);
        }
        // 删除浏览器的cookie
        CookieUtils.deleteCookie(prop.getUser().getCookieName(), prop.getUser().getCookieDomain(), response);
        log.info("【授权中心】用户退出成功，并且设置了黑名单，token的id = {}", payload.getId());
    }

    /**
     * 给微服务申请token
     * @param id 微服务自己的id
     * @param secret 微服务自己的密码
     * @return
     */
    public String authorization(Long id, String secret) {
        // 去数据库中查询有无这个服务
        ApplicationInfo app = infoMapper.selectByPrimaryKey(id);
        // 判空
        if(app == null){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        // 不为空需要校验密码
        if(!passwordEncoder.matches(secret, app.getSecret())){
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
        // 如果密码正确，我们去查询当前微服务有访问那些服务的权限： target_id
        List<Long> targetIdList = infoMapper.queryTargetIdListByServiceId(id);
        // 封装载荷对象
        AppInfo appInfo = new AppInfo(id, app.getServiceName(), targetIdList);
        // 生成token： 25小时之后失效
        return JwtUtils.generateTokenExpireInMinutes(appInfo, prop.getPrivateKey(), prop.getApp().getExpire());
    }
}
