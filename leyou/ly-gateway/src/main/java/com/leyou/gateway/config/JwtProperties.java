package com.leyou.gateway.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

/**
 * 公钥： 除了授权中心之外，其他的微服务只能持有公钥
 */
@Data
@Slf4j
@ConfigurationProperties("ly.jwt")
public class JwtProperties implements InitializingBean {
    private String pubKeyPath;
    private PublicKey publicKey; // 公钥
    /**跟用户登录token相关的属性*/
    private UserTokenProperties user = new UserTokenProperties();//一定要实例化

    //微服务申请token的id和密码
    private AppTokenProperties app = new AppTokenProperties();


    @Data
    public class AppTokenProperties {
        private Long id;
        private String secret;
        private String headerName;
    }
    @Data
    public class UserTokenProperties {
        /**存放token的cookie名称*/
        private String cookieName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            log.info("【乐优网关】加载公钥成功");
        } catch (Exception e) {
            log.error("【乐优网关】加载公钥失败,原因：{}", e.getMessage());
            throw new RuntimeException("公钥加载失败",e);
        }
    }
}
