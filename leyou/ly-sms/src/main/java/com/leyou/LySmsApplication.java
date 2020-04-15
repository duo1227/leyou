package com.leyou;

import com.leyou.sms.config.SmsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SmsProperties.class)
public class LySmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(LySmsApplication.class,args);
    }
}
