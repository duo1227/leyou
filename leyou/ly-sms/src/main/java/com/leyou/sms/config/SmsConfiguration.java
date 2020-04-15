package com.leyou.sms.config;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SmsConfiguration {

    @Bean
   public IAcsClient acsClient (SmsProperties prop){
        DefaultProfile profile = DefaultProfile.getProfile(
                prop.getRegionID(),
                prop.getAccessKeyID(),
                prop.getAccessKeySecret());
        return new DefaultAcsClient(profile);
    }
}
