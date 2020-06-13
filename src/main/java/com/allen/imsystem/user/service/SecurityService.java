package com.allen.imsystem.user.service;

import org.springframework.stereotype.Service;

/**
 * 安全验证相关的业务逻辑接口
 */
@Service
public interface SecurityService {

    /**
     * 发送邮箱确认验证码
     */
    boolean sendCheckEmail(Integer type, String email);

    /**
     * 验证邮箱验证码
     */
    boolean verifyEmailCode(String emailCode, String emailCodeToken);

    boolean verifyImageCode(String imageCode, String correctImageCode);

}
