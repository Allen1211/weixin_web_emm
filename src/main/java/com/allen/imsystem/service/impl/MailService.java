package com.allen.imsystem.service.impl;

import com.allen.imsystem.common.exception.BusinessException;
import com.allen.imsystem.common.exception.ExceptionType;
import com.allen.imsystem.model.message.EmailMessage;
import com.allen.imsystem.service.IMailService;
import javafx.fxml.Initializable;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
@Service
public class MailService implements IMailService, MessageListener {

    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private VelocityEngine velocityEngine;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private static final String PROP_FILE_LOCATION = "classpath:mail/mail.properties";
    private static final String TEMPLATE_LOCATION = "velocity.vm";

    @Value("${mail.smtp.username}")
    private String FROM;
    @Value("${mail.smtp.defaultEncoding}")
    private String ENCODING;


    @Override
    public void onMessage(Message message, byte[] pattern) {
        RedisSerializer<EmailMessage> serializer = (RedisSerializer<EmailMessage>) redisTemplate.getDefaultSerializer();
        EmailMessage emailMessage = serializer.deserialize(message.getBody());
        boolean success = this.sendMailVelocity(emailMessage.getSubject(),emailMessage.getFileLocation(),
                emailMessage.getEmailAddress(),emailMessage.getModel());
        if(!success){
            throw new BusinessException(ExceptionType.SERVER_ERROR, "发送邮件失败");
        }
    }

    @Override
    public boolean sendMailVelocity(String subject, String location, String emailAddress, Map<String,Object> model) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage,true,ENCODING);
            helper.setFrom(FROM);       // 发送方
            helper.setTo(emailAddress); // 接收方
            helper.setSubject(subject); // 主题

            //设置文本内容
            String content = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,TEMPLATE_LOCATION,ENCODING, model);
            helper.setText(content,true);
            //设置附件
            if(location != null){
                File file = FileUtils.getFile(location);
                FileSystemResource resource = new FileSystemResource(file);
                helper.addAttachment(file.getName(),resource);
            }
            //发送
            javaMailSender.send(mimeMessage);

        }catch (MessagingException e){
            e.printStackTrace();
        }
        return true;
    }


}
