package com.allen.imsystem.service;

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface IMailService {



    /**
     *  使用velocity模板引擎发送邮件接口
     * @param subject   标题
     * @param location  附件地址, 为null不发送附件
     * @param emailAddress  接收方邮箱地址
     * @return
     */
    boolean sendMailVelocity(String subject, String location, String emailAddress, Map<String,Object> model);
}
