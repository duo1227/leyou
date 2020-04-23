package com.leyou.auth.task;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.mapper.ApplicationInfoMapper;
import com.leyou.common.auth.entity.AppInfo;
import com.leyou.common.auth.utils.JwtUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

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
    private JwtProperties prop;

    @Autowired
    private ApplicationInfoMapper infoMapper;

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
                log.info("【授权中心】自己给自己申请token开始");
                // 如果密码正确，我们去查询当前微服务有访问那些服务的权限： target_id
                List<Long> targetIdList = infoMapper.queryTargetIdListByServiceId(prop.getApp().getId());
                // 封装载荷对象
                AppInfo appInfo = new AppInfo(prop.getApp().getId(), prop.getApp().getName(), targetIdList);
                // 生成token： 25小时之后失效
                token = JwtUtils.generateTokenExpireInMinutes(appInfo, prop.getPrivateKey(), prop.getApp().getExpire());
                log.info("【授权中心】自己给自己申请token成功");
                // 如果申请成功跳出循环
                break;
            } catch (Exception e) {
                log.error("【授权中心】自己给自己申请token失败：{}", e.getMessage());
            }
            Thread.sleep(REFRESH_TOKEN_SLEEP_TIME);
        }
    }
}
