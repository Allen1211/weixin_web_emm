package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.message.EmailMessage;
import com.allen.imsystem.service.ISecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class SecurityService implements ISecurityService {

    @Qualifier("AttrCacheHolder")
    @Autowired
    ICacheHolder cacheHolder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public boolean sendCheckEmail(Integer type, String email) {
        Map<String, Object> model = new HashMap<>(2);
        Random random = new Random(System.currentTimeMillis());
        String emailCode = String.valueOf(random.nextInt(899999) + 100000);
        model.put("emailCode", emailCode);
        model.put("sendTime", new Date());
        String subject;
        switch (type) {
            case 1:
                subject = "ImSystem注册邮箱验证";
                break;
            case 2:
                subject = "ImSystem找回密码邮箱验证";
                break;
            default:
                throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL,"不支持的type类型");
        }
        // 发布到消息队列
        EmailMessage emailMessage = new EmailMessage(subject, email, null, model);
        redisTemplate.convertAndSend("email", emailMessage);

        // 验证码缓存
        Long expriedTime = System.currentTimeMillis() + 20 * 60 * 1000; // 过期时间20分钟
        String emailCodeToken = emailCode + "#" + expriedTime;  // 拼接
        cacheHolder.setEmailCode(emailCodeToken, email);
        return true;
    }

    @Override
    public boolean verifyImageCode(String imageCode, String correctImageCode) {
//        if(imageCode==null || !StringUtils.equals(imageCode,correctImageCode)){
//            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "验证码错误");
//        }
        return true;
    }

    @Override
    public boolean verifyEmailCode(String emailCode, String emailCodeToken) {
        System.out.println("emailCode->" + emailCode);
        System.out.println("token->" + emailCodeToken);

        /**
         * 邮箱验证码校验逻辑
         */
        if (emailCodeToken == null) {
            throw new BusinessException(ExceptionType.EMAIL_CODE_WRONG, "邮箱验证码输入错误");
        }
        String[] var = emailCodeToken.split("#");
        String correctEmailCode = var[0];
        Long expriedTime = Long.valueOf(var[1]);

        if (!emailCode.equals(correctEmailCode)) {
            throw new BusinessException(ExceptionType.EMAIL_CODE_WRONG, "邮箱验证码输入错误");
        } else if (System.currentTimeMillis() > expriedTime) {
            throw new BusinessException(ExceptionType.EMAIL_CODE_WRONG, "邮箱验证码已过期，请重新获取");
        }
        return true;
    }


}
