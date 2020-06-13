package com.allen.imsystem.common.config;

import com.allen.imsystem.user.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.ui.velocity.VelocityEngineFactoryBean;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("mail.smtp.host")
    private String host;

    @Value("mail.smtp.username")
    private String username;

    @Value("mail.smtp.password")
    private String password;

    @Value("mail.smtp.defaultEncoding")
    private String defaultEncoding;

    @Value("mail.smtp.auth")
    private String auth;

    @Value("mail.smtp.timeout")
    private String timeout;

    @Value("mail.smtp.socketFactory.class")
    private String socketFactoryClass;

    @Value("mail.smtp.port")
    private String port;

    @Autowired
    private MailService mailService;

    @Bean
    public JavaMailSender javaMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(host);
        javaMailSender.setPassword(password);
        javaMailSender.setDefaultEncoding(defaultEncoding);
        javaMailSender.setUsername(username);
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.auth",auth);
        properties.setProperty("mail.smtp.timeout",auth);
        properties.setProperty("mail.smtp.socketFactory.class",socketFactoryClass);
        properties.setProperty("mail.smtp.port",port);
        javaMailSender.setJavaMailProperties(properties);

        return javaMailSender;
    }

    @Bean
    public VelocityEngineFactoryBean velocityEngineFactoryBean(){
        VelocityEngineFactoryBean velocityEngineFactoryBean = new VelocityEngineFactoryBean();
        velocityEngineFactoryBean.setResourceLoaderPath("classpath:mail/");
        Properties properties = new Properties();
        properties.setProperty("input.encoding","UTF-8");
        properties.setProperty("output.encoding","UTF-8");
        properties.setProperty("contentType","ext/html;charset=UTF-8");
        properties.setProperty("loopCounter","loopCounter");
        properties.setProperty("directive.foreach.counter.initial.value","0");
        velocityEngineFactoryBean.setVelocityProperties(properties);
        return velocityEngineFactoryBean;
    }

    @Bean
    RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener((MessageListener) mailService, new ChannelTopic("email"));
        return container;
    }
}
