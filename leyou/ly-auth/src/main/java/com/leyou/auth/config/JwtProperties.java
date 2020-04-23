package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {

    private String pubKeyPath;
    private String priKeyPath;

    private PublicKey publicKey; //公钥
    private PrivateKey privateKey; //私钥

    /**
     * 跟用户登录token相关的属性
     */
    private UserTokenProperties user = new UserTokenProperties();//一定要实例化

    /**
     * 微服务过期时间设置
     */
    private AppTokenProperties app = new AppTokenProperties();

    @Data
    public class AppTokenProperties{
        /**
         * 微服务过期时间设置,单位分钟
         */
        private int expire;

        /**
         * auth服务的id
         */
        private Long id;

        /**
         * 服务名称
         */
        private String name;

        private String headerName;
    }

    @Data
    public class UserTokenProperties {
        /**
         * 过期时间，单位分钟
         */
        private int expire;
        /**
         * 存放token的cookie名称
         */
        private String cookieName;
        /**
         * 存放token的cookie的domain
         */
        private String cookieDomain;
        /**
         * 刷新时间
         */
        private int RefreshTime;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
            log.info("【授权中心】加载公钥和私钥成功");
        } catch (Exception e) {
            log.error("【授权中心】加载公钥和私钥失败,原因：{}", e.getMessage());
            throw new RuntimeException("公钥和私钥加载失败",e);
        }
    }
}