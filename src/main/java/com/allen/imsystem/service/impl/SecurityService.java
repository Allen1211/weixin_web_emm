package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.service.ISecurityService;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.mail.Session;

@Service
public class SecurityService implements ISecurityService {

    @Override
    public boolean verifyImageCode(String imageCode, String correctImageCode) {
        if(imageCode==null || !StringUtils.equals(imageCode,correctImageCode)){
            throw new BusinessException(ExceptionType.PARAMETER_ILLEGAL, "验证码错误");
        }
        return true;
    }

    @Override
    public boolean verifyEmailCode(String emailCode, String emailCodeToken) {
        System.out.println("emailCode->"+emailCode);
        System.out.println("token->"+emailCodeToken);

        /**
         * 邮箱验证码校验逻辑
         */
        if(emailCodeToken == null){
            throw new BusinessException(ExceptionType.EMAIL_CODE_WRONG, "邮箱验证码输入错误");
        }
        String[] var = emailCodeToken.split("#");
        String correctEmailCode = var[0];
        Long expriedTime = Long.valueOf(var[1]);

        if(! emailCode.equals(correctEmailCode)){
            throw new BusinessException(ExceptionType.EMAIL_CODE_WRONG, "邮箱验证码输入错误");
        }else if(System.currentTimeMillis() > expriedTime){
            throw new BusinessException(ExceptionType.EMAIL_CODE_WRONG, "邮箱验证码已过期，请重新获取");
        }
        return true;
    }


}
