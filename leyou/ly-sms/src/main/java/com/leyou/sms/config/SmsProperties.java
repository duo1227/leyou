package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.sms")
public class SmsProperties {
    String accessKeyID; //账号
    String accessKeySecret;//密钥
    String signName;//短信签名
    String verifyCodeTemplate;//短信模板
    String domain;//发送短信请求的域名
    String version;//API版本
    String action;//API类型
    String regionID;//区域
}