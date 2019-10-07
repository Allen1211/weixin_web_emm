package com.allen.imsystem.common;

import com.allen.imsystem.common.Const.GlobalConst;
import com.allen.imsystem.common.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.List;

@Component
public class EmailServiceMessageConsumer implements Runnable {

    @Autowired
    private RedisUtil redisUtil;
    private Jedis jedis;
    private volatile int count = 0;

    public EmailServiceMessageConsumer() {
    }

    public void consumeMessage(){
        if(jedis == null){
            jedis = redisUtil.getResource();
        }
        System.out.println("start");
        List<String> message =  jedis.brpop(0, GlobalConst.MQ.EMAIL_MESSAGE_KEY);
        System.out.println(message.get(0) + "->" + message.get(1));
        count++;
    }

    @Override
    public void run() {
        System.out.println("started");
        while (count < 4){
            consumeMessage();
        }
        jedis.close();
    }


}
