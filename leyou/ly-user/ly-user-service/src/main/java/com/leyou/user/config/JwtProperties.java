package com.leyou.user.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.PublicKey;

@Data
@Slf4j
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {
    /**
     * 公钥地址
     */
    private String pubKeyPath;
    /**
     * 服务认证token相关属性
     */
    private PrivilegeTokenProperties app = new PrivilegeTokenProperties();

    private PublicKey publicKey;

    @Data
    public class PrivilegeTokenProperties{
        /**
         * 服务id
         */
        private Long id;
        /**
         * 服务密钥
         */
        private String secret;
        /**
         * 存放服务认证token的请求头
         */
        private String headerName;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            // 获取公钥和私钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}