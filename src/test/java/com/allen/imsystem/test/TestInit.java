package com.allen.imsystem.test;

import com.allen.imsystem.common.utils.SpringBeanUtil;
import com.allen.imsystem.common.utils.UidGenerator;
import com.allen.imsystem.dao.UserDao;
import com.allen.imsystem.service.IMailService;
import com.allen.imsystem.service.IUserService;
import com.allen.imsystem.service.impl.MailService;
import com.allen.imsystem.service.impl.UserService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext.xml"})
public class TestInit {

    @Autowired
    IUserService userService;

    @Autowired
    MailService mailService;

    @Autowired
    UserDao userDao;

    @Autowired
    UidGenerator uidGenerator;


    @Test
    public void testSpringBeanUtil(){
        System.out.println(SpringBeanUtil.getBean(IUserService.class));
    }

    @Test
    public void testMailService(){
        Assert.assertNotNull(mailService);
//        System.out.println(mailService.getFROM());
//        System.out.println(mailService.getENCODING());
//        System.out.println(mailService.getJavaMailSender());
//        System.out.println(mailService.getVelocityEngine());
    }

    @Test
    public void testIdGenerator(){
        uidGenerator.generateUid();
    }
}
