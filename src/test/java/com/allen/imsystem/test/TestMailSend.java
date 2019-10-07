package com.allen.imsystem.test;

import com.allen.imsystem.service.IMailService;
import com.allen.imsystem.service.IUserService;
import com.allen.imsystem.service.impl.MailService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext.xml"})
public class TestMailSend {

    @Autowired
    IUserService userService;

    @Autowired
    IMailService mailService;

    @Test
    public void test1(){
        //参数map
        Map<String,Object> paramMap = new HashMap<>();
        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        paramMap.put("sendTime",dateStr);
        paramMap.put("emailCode",12345);
        boolean success = mailService.sendMailVelocity("测试1",null, "837806944@qq.com",paramMap);
        Assert.assertTrue(success);
    }
    @Test
    public void test2(){
        InputStream is = this.getClass().getResourceAsStream("/mail/velocity.vm");
        Assert.assertNotNull(is);

    }

}
