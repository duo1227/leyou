package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.RegexUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.Queue.SMS_VERIFY_CODE_QUEUE;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;


@Slf4j
@Component
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    /**
     * 发送短信
     * @param msg
     */
    @RabbitListener(bindings = @QueueBinding(
            value=@Queue(value = SMS_VERIFY_CODE_QUEUE, durable = "true"),
            exchange = @Exchange(name = SMS_EXCHANGE_NAME,type = ExchangeTypes.TOPIC),
            key = VERIFY_CODE_KEY
    ))

    /**
     *  传进来一个phone 手机号码，一个code 验证码
     */
    public void sendVerifyCode(Map<String, String> msg){
        try {
            // 1、判空
            if(CollectionUtils.isEmpty(msg)){
                log.error("【短信微服务】发送短信失败：短信内容为空");
                return ;
            }
            // 2、取出手机号并验证
            String phone = msg.get("phone");
            if(!RegexUtils.isPhone(phone)){
                log.error("【短信微服务】发送短信失败：手机号码不符合规范");
                return ;
            }
            // 3、取出验证码并验证
            String code = msg.get("code");
            if(!RegexUtils.isVerifyCode(code)){
                log.error("【短信微服务】发送短信失败：验证码不符合规范");
                return ;
            }
            // 4、发送短信
            smsUtils.sendMessage(phone, prop.getSignName(), prop.getVerifyCodeTemplate(), JsonUtils.toString(msg));
            log.info("【短信微服务】发送短信成功：发送的手机号: {}",phone);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("【短信微服务】发送短信失败：原因：{}",e.getMessage());
        }

    }

}
