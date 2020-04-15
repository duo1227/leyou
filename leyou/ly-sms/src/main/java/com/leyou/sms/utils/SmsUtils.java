package com.leyou.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.leyou.sms.config.SmsConstants.*;

/**
 * 发短信工具类
 */
@Slf4j
@Component
public class SmsUtils {

    @Autowired
    private IAcsClient acsClient;

    @Autowired
    private SmsProperties prop;


    /**
     * 发送短信的工具类
     * @param phoneNumber   手机号
     * @param signName      签名
     * @param templateCode  模板代码
     * @param templateParam 模板参数
     */
    public void sendMessage(String phoneNumber, String signName, String templateCode, String templateParam){
        CommonRequest request = new CommonRequest();

        request.setMethod(MethodType.POST);
        request.setDomain(prop.getDomain());
        request.setVersion(prop.getVersion());
        request.setAction(prop.getAction());
        request.putQueryParameter(SMS_PARAM_REGION_ID, prop.getRegionID());
        request.putQueryParameter(SMS_PARAM_KEY_PHONE, phoneNumber);
        request.putQueryParameter(SMS_PARAM_KEY_SIGN_NAME, signName);
        request.putQueryParameter(SMS_PARAM_KEY_TEMPLATE_CODE, templateCode);
        request.putQueryParameter(SMS_PARAM_KEY_TEMPLATE_PARAM, templateParam);
        try {
            CommonResponse response = acsClient.getCommonResponse(request);
            if(response.getHttpStatus() >= 300){
                log.error("【SMS服务】发送短信失败。响应信息：{}", response.getData());
            }
            // 获取响应体
            Map<String, String> resp = JsonUtils.toMap(response.getData(), String.class, String.class);
            // 判断是否是成功
            if(!StringUtils.equals(OK, resp.get(SMS_RESPONSE_KEY_CODE))){
                // 不成功，
                log.error("【SMS服务】发送短信失败，原因{}", resp.get(SMS_RESPONSE_KEY_MESSAGE));
                throw new LyException(ExceptionEnum.SEND_MESSAGE_ERROR);
            }
            log.info("【SMS服务】发送短信成功，手机号：{}, 响应：{}", phoneNumber, response.getData());
        } catch (ServerException e) {
            log.error("【短信服务】发送短信失败，阿里云服务端异常，{}",e.getMessage(),e);
            throw new LyException(ExceptionEnum.SEND_MESSAGE_ERROR);
        } catch (ClientException e) {
            log.error("【短信服务】发送短信失败，客户端异常，{}",e.getMessage(),e);
            throw new LyException(ExceptionEnum.SEND_MESSAGE_ERROR);
        }
    }

}
