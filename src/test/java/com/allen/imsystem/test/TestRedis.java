package com.allen.imsystem.test;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.EmailServiceMessageConsumer;
import com.allen.imsystem.common.ICacheHolder;
import com.allen.imsystem.common.utils.JWTUtil;
import com.allen.imsystem.common.utils.RedisUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:spring/springmvc.xml", "classpath*:spring/applicationContext.xml"})
public class TestRedis {

    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void testJedisConnection(){
        Jedis jedis = new Jedis("120.77.42.156",6379);
        jedis.auth("123456");
        Assert.assertEquals("PONG",jedis.ping());
    }

    @Test
    public void testJedisGetSet(){
        Jedis jedis = redisUtil.getResource();
        Assert.assertEquals("PONG",jedis.ping());

        Assert.assertTrue(redisUtil.set("1","a"));
        Assert.assertTrue(redisUtil.set("2","b"));
        Assert.assertEquals("a",redisUtil.get("1"));
        Assert.assertEquals("b",redisUtil.get("2"));
        Assert.assertTrue(redisUtil.remove("1"));

    }

    @Test
    public void testJedisHGetHSet(){
        Jedis jedis = redisUtil.getResource();
        Assert.assertEquals("PONG",jedis.ping());
        jedis.close();
        Assert.assertTrue(redisUtil.hset("userStatus","allen","1"));
        Assert.assertTrue(redisUtil.hset("userStatus","mike","2"));
        Assert.assertTrue(redisUtil.hset("table","allen","3"));
        Assert.assertTrue(redisUtil.hset("table","allen","4"));
        Assert.assertEquals("1",redisUtil.hget("userStatus","allen"));
        Assert.assertEquals("2",redisUtil.hget("userStatus","mike"));
        Assert.assertEquals("4",redisUtil.hget("table","allen"));
        Assert.assertTrue(redisUtil.remove("userStatus"));
        Assert.assertTrue(redisUtil.remove("table"));

    }

    @Test
    public void testRedisMQ(){
//        new Thread(new EmailServiceMessageConsumer(),"consumer").start();
//        Jedis jedis = redisUtil.getResource();
//        for(int i=1;i<=15;i++){
//            jedis.lpush(GlobalConst.MQ.EMAIL_MESSAGE_KEY, "from producer1 Message"+i);
//        }
//        jedis.close();


//    Jedis jedis = redisUtil.getResource();
//    for(int i=1;i<=5;i++){
//        System.out.println("lpush");
//        jedis.lpush(GlobalConst.MQ.EMAIL_MESSAGE_KEY, "from producer1 Message"+i);
//    }
//    jedis.close();

    while (true){

    }
//        new Thread(() -> {
//            Jedis jedis = redisUtil.getResource();
//            for(int i=1;i<=5;i++){
//                jedis.lpush(GlobalConst.MQ.EMAIL_MESSAGE_KEY, "from producer2 Message"+i);
//            }
//            jedis.close();
//        },"producer2").start();
    }

    @Qualifier("defaultCacheHolder")
    @Autowired
    private ICacheHolder cacheHolder;

    @Test
    public void testCache1(){
        Assert.assertTrue(cacheHolder.setImageCode("abcd","12345678"));
        Assert.assertTrue(cacheHolder.setImageCode("test","12345"));
        Assert.assertEquals("abcd",cacheHolder.getImageCode("12345678"));
        Assert.assertEquals("test",cacheHolder.getImageCode("12345"));
        Assert.assertEquals(null,cacheHolder.getEmailCode("12345678"));
        cacheHolder.removeImageCode("12345678");
        Assert.assertEquals(null,cacheHolder.getImageCode("12345678"));
    }

    @Test
    public void testCache2(){
        String token = JWTUtil.createLoginToken("123",555,60*60);

        long a = System.currentTimeMillis();

        String uid = JWTUtil.getMsgFromToken(token,"uid",String.class);
        int userId = JWTUtil.getMsgFromToken(token,"userId",Integer.class);

        long b = System.currentTimeMillis();

        System.out.println(b-a + " ms");

        Assert.assertEquals("123",uid);
        Assert.assertEquals(555,userId);
    }
}
