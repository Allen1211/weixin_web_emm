package com.allen.imsystem.test;

import com.allen.imsystem.model.pojo.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext.xml"})
public class TestSDR {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    public void testInject(){
        Assert.assertNotNull(redisTemplate);
        Assert.assertNotNull(stringRedisTemplate);
    }
    @Test
    public void test1(){
        ListOperations<String,Object> listOperations = redisTemplate.opsForList();
        Assert.assertEquals("temp",listOperations.rightPop("temp"));
//        listOperations.leftPush("email_task","123");
    }
    @Test
    public void test2(){
        ListOperations<String,String> listOperationsForString = stringRedisTemplate.opsForList();
        listOperationsForString.leftPush("email_task","123");
        Assert.assertEquals("123",listOperationsForString.rightPopAndLeftPush("email_task","temp"));
        ListOperations<String,Object> listOperations = redisTemplate.opsForList();
        Assert.assertEquals("123",listOperationsForString.rightPop("temp"));
    }
    @Test
    public void test3(){
        HashOperations<String,String,Object> hashOperations = redisTemplate.opsForHash();
        User user = new User();
        user.setId(1);
        user.setUid("123");
        hashOperations.put("user","allen",user);
        User user1 = (User) hashOperations.get("user","allen");
        Assert.assertNotNull(user1);
        Assert.assertEquals("123",user1.getUid());
    }

    @Test
    public void test4(){
    }

    @Test
    public void testPubSub() throws ClassNotFoundException, InterruptedException {
        redisTemplate.convertAndSend("email","hello");
    }
}
