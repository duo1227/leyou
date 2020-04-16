package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.RegexUtils;
import com.leyou.user.dto.UserDTO;
import com.leyou.user.entity.User;
import com.leyou.user.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.leyou.common.constants.MQConstants.Exchange.SMS_EXCHANGE_NAME;
import static com.leyou.common.constants.MQConstants.RoutingKey.VERIFY_CODE_KEY;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    /**
     * 数据校验
     * @param data
     * @param type
     * @return
     */
    public Boolean check(String data, Integer type) {

        User user = new User();
        switch (type){
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }


        return userMapper.selectCount(user) == 0;

    }

    private static final String KEY_PREFIX = "user:verify:phone:";
    /**
     * 根据用户手机号码发送短信验证码
     * @param phone
     */
    public void sendCode(String phone) {

        //1.判断手机正确性
        if (!RegexUtils.isPhone(phone)){
            throw new LyException(ExceptionEnum.INVALID_PHONE_NUMBER);
        }

        //2.生成6位随机验证码
        String code = RandomStringUtils.randomNumeric(6);

        //3.存入redis中
        redisTemplate.opsForValue().set(KEY_PREFIX+phone,code,5, TimeUnit.MINUTES);

        //4.发消息
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);

        amqpTemplate.convertAndSend(SMS_EXCHANGE_NAME,VERIFY_CODE_KEY,msg);

    }

    /**
     * 用户注册
     * @param user 用户数据
     * @param code 验证码
     */
    public void register(User user, String code) {

        //1.从redis拿出验证码对比
        String cacheVerifyCode = redisTemplate.opsForValue().get(KEY_PREFIX + user.getPhone());
        if (!StringUtils.equals(code,cacheVerifyCode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }

        //2.对密码加密
        if (StringUtils.isBlank(user.getPassword())){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //3.插入数据库
        int count = userMapper.insertSelective(user);
        if (count != 1){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    public UserDTO queryUserByUserNameAndPassword(String username, String password) {

        //1.先根据用户名查询用户信息
        User record = new User();
        record.setUsername(username);

        User user = userMapper.selectOne(record);
        if (user == null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        //2.判断密码
        //解密:
        // 1.明文  2.加密的密码
        //返回值是boolean true是一致，false不一致
        if (!passwordEncoder.matches(password,user.getPassword())){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        //3.返回userDto
        UserDTO userDTO = BeanHelper.copyProperties(user, UserDTO.class);
        return userDTO;
    }
}
