package com.leyou.gateway.task;

import com.leyou.gateway.client.AuthClient;
import com.leyou.gateway.config.JwtProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 这个类的作用： 定时去授权中心申请token
 *      1）定时任务： 在启动类中必须开启定时任务：@EnableScheduling
 *      2）类上必须有一个 @Component注解
 *      3）编写一个定时任务的方法，方法上必须有这个注解：@Scheduled
 */
@Slf4j
@Component
public class AppTokenHolder {

    private final static long APP_TOKEN_REFRESH_TIME = 86400000L; // 每隔24小时去申请一次token
    private final static long REFRESH_TOKEN_SLEEP_TIME = 10000L; // 出现了异常等10秒再去申请

    @Autowired
    private AuthClient authClient;

    @Autowired
    private JwtProperties prop;

    // 微服务申请好的token就保存在这里
    @Getter
    private String token;

    /**
     * 我们固定每24小时就去申请一次新的token，我们原来申请的token他的有效期是25小时。
     */
    @Scheduled(fixedDelay = APP_TOKEN_REFRESH_TIME)
    public void loadAppToken() throws InterruptedException {
        while (true){
            try {
                log.info("【乐优网关】去授权中心申请token开始");
                token = authClient.authorization(prop.getApp().getId(), prop.getApp().getSecret());
                log.info("【乐优网关】去授权中心申请token成功");
                // 如果申请成功跳出循环
                break;
            } catch (Exception e) {
                log.error("【乐优网关】去授权中心申请token失败：{}", e.getMessage());
            }
            Thread.sleep(REFRESH_TOKEN_SLEEP_TIME);
        }
    }
}
